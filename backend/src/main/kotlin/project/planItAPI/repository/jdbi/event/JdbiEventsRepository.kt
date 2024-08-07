package project.planItAPI.repository.jdbi.event

import org.jdbi.v3.core.Handle
import project.planItAPI.domain.event.Coordinates
import project.planItAPI.models.EventModel
import project.planItAPI.domain.event.Money
import project.planItAPI.models.FindNearbyEventsOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.models.SearchEventsOutputModel
import project.planItAPI.models.UserInEvent
import project.planItAPI.models.UsersInEventList
import java.sql.Timestamp

class JdbiEventsRepository (private val handle: Handle): EventsRepository {

    override fun createEvent(
        title: String,
        description: String,
        category: String,
        locationType: String?,
        location: String?,
        latitude: Double?,
        longitude: Double?,
        visibility: String,
        date: Timestamp?,
        end_date: Timestamp?,
        price: Money?,
        userID: Int,
        password: String,
        eventCode: String
    ): Int? {
        val eventId = handle.createUpdate(
            "insert into dbo.event(title, description, category, locationType, location, latitude, longitude, " +
                    "visibility, date, end_date, priceAmount, priceCurrency, password, code) values " +
                    "(:title, :description, :category, CAST(:locationType AS dbo.locationtype), :location, " +
                    ":latitude, :longitude, CAST(:visibility AS dbo.visibilitytype), :date, :end_date, :priceAmount, " +
                    ":priceCurrency, :password, :code)"
        )
            .bind("title", title)
            .bind("description", description)
            .bind("category", category)
            .bind("locationType", locationType)
            .bind("location", location)
            .bind("latitude", latitude)
            .bind("longitude", longitude)
            .bind("visibility", visibility)
            .bind("date", date)
            .bind("end_date", end_date)
            .bind("priceAmount", price?.amount)
            .bind("priceCurrency", price?.currency)
            .bind("password", password)
            .bind("code", eventCode)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()

        handle.createUpdate(
            "insert into dbo.UserParticipatesInEvent(user_id, event_id) " +
                    "values (:user_id, :event_id)"
        )
            .bind("user_id", userID)
            .bind("event_id", eventId)
            .execute()

        handle.createUpdate(
            "insert into dbo.Roles(name, event_id, user_id)" +
                    "values (:name, :event_id, :user_id)"
        )
            .bind("name", "Organizer")
            .bind("event_id", eventId)
            .bind("user_id", userID)
            .execute()

        return eventId
    }

    override fun getEvent(id: Int): EventModel? {
        return handle.createQuery(
            "select * from dbo.Event where id = :id"
        )
            .bind("id", id)
            .mapTo(EventModel::class.java)
            .singleOrNull()
    }

    override fun getEventByCode(eventCode: String): EventModel? {
        return handle.createQuery(
            "select * from dbo.Event where code = :code"
        )
            .bind("code", eventCode)
            .mapTo(EventModel::class.java)
            .singleOrNull()
    }

    override fun getUsersInEvent(id: Int): UsersInEventList? {
        return handle.createQuery(
            """
        SELECT u.id, u.username, r.name as roleName, r.id as roleId, u.name
        FROM dbo.UserParticipatesInEvent up
        JOIN dbo.Users u ON up.user_id = u.id
        LEFT JOIN dbo.Roles r ON r.user_id = u.id AND r.event_id = up.event_id
        WHERE up.event_id = :id
        """
        )
            .bind("id", id)
            .mapTo(UserInEvent::class.java)
            .list()
            .let { UsersInEventList(it) }
    }

    override fun searchEvents(searchInput: String, limit: Int, offset: Int): SearchEventListOutputModel {
        return handle.createQuery(
            """
        SELECT id, title, description, category, location, latitude, longitude, visibility, date
        FROM dbo.Event
        WHERE title LIKE :searchInput
        LIMIT :limit OFFSET :offset
        """
        )
            .bind("searchInput", "%$searchInput%")
            .bind("limit", limit)
            .bind("offset", offset)
            .mapTo(SearchEventsOutputModel::class.java)
            .list()
            .let { SearchEventListOutputModel(it) }
    }

    override fun searchEventsByCategory(category: String, limit: Int, offset: Int): SearchEventListOutputModel {
        return handle.createQuery(
            """
        SELECT id, title, description, category, location, latitude, longitude, visibility, date
        FROM dbo.Event
        WHERE category = :category AND visibility = 'Public'
        LIMIT :limit OFFSET :offset
        """
        )
            .bind("category", category)
            .bind("limit", limit)
            .bind("offset", offset)
            .mapTo(SearchEventsOutputModel::class.java)
            .list()
            .let { SearchEventListOutputModel(it) }
    }

    override fun getNearbyEvents(userCoords: Coordinates, radius: Int, limit: Int, userId: Int): List<FindNearbyEventsOutputModel> {
        return handle.createQuery(
            """
        SELECT id, title, location, latitude, longitude
        FROM dbo.Event
       WHERE (
            6371 * acos(
                cos(radians(:userLatitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLongitude)) +
                sin(radians(:userLatitude)) * sin(radians(latitude))
            )
        ) * 1000 <= :radius
        AND visibility = 'Public'
        AND id NOT IN (SELECT event_id FROM dbo.UserParticipatesInEvent WHERE user_id = :userId)
        LIMIT :limit
        """
        )
            .bind("userLongitude", userCoords.longitude)
            .bind("userLatitude", userCoords.latitude)
            .bind("radius", radius)
            .bind("userId", userId)
            .bind("limit", limit)
            .mapTo(FindNearbyEventsOutputModel::class.java)
            .list()
    }

    override fun getAllEvents(limit: Int, offset: Int): SearchEventListOutputModel {
        return handle.createQuery(
            "select id, title, description, category, location, latitude, longitude, visibility, date " +
                    "from dbo.Event " +
                    "LIMIT :limit OFFSET :offset"

        )
            .bind("limit", limit)
            .bind("offset", offset)
            .mapTo(SearchEventsOutputModel::class.java)
            .list()
            .let { SearchEventListOutputModel(it) }
    }

    override fun joinEvent(userId: Int, eventId: Int) {
        handle.createUpdate(
            "insert into dbo.UserParticipatesInEvent(user_id, event_id) " +
                    "values (:user_id, :event_id)"
        )
            .bind("user_id", userId)
            .bind("event_id", eventId)
            .execute()

        handle.createUpdate(
            "insert into dbo.Roles(name, event_id, user_id)" +
                    "values (:name, :event_id, :user_id)"
        )
            .bind("name", "Participant")
            .bind("event_id", eventId)
            .bind("user_id", userId)
            .execute()
    }

    override fun kickUserFromEvent(userId: Int, eventId: Int) {
        handle.createUpdate(
            "delete from dbo.Roles where user_id = :user_id and event_id = :event_id"
        )
            .bind("user_id", userId)
            .bind("event_id", eventId)
            .execute()

        handle.createUpdate(
            "delete from dbo.UserParticipatesInEvent where user_id = :user_id and event_id = :event_id"
        )
            .bind("user_id", userId)
            .bind("event_id", eventId)
            .execute()
    }

    override fun deleteEvent(eventId: Int) {
        handle.createUpdate(
            "delete from dbo.Roles where event_id = :eventId"
        )
            .bind("eventId", eventId)
            .execute()

        handle.createUpdate(
            "delete from dbo.UserParticipatesInEvent where event_id = :eventId"
        )
            .bind("eventId", eventId)
            .execute()


        handle.createUpdate(
            "delete from dbo.Event where id = :eventId"
        )
            .bind("eventId", eventId)
            .execute()
    }

    override fun editEvent(
        eventId: Int,
        title: String?,
        description: String?,
        category: String?,
        locationType: String?,
        location: String?,
        latitude: Double?,
        longitude: Double?,
        visibility: String?,
        date: Timestamp?,
        end_date: Timestamp?,
        price: Money?,
        password: String
    ) {
        handle.createUpdate(
            "update dbo.Event set " +
                    "title = :title, " +
                    "description = :description, " +
                    "category = :category, " +
                    "locationType = CAST(:locationType AS dbo.locationtype)," +
                    "location = :location, " +
                    "latitude = :latitude, " +
                    "longitude = :longitude, " +
                    "visibility = CAST(:visibility AS dbo.visibilitytype), " +
                    "date = :date, " +
                    "end_date = :end_date, " +
                    "priceAmount = :priceAmount, " +
                    "priceCurrency = :priceCurrency, " +
                    "password = :password " +
                    "where id = :eventId"
        )
            .bind("title", title)
            .bind("description", description)
            .bind("category", category)
            .bind("locationType", locationType)
            .bind("location", location)
            .bind("latitude", latitude)
            .bind("longitude", longitude)
            .bind("visibility", visibility)
            .bind("date", date)
            .bind("end_date", end_date)
            .bind("priceAmount", price?.amount)
            .bind("priceCurrency", price?.currency)
            .bind("password", password)
            .bind("eventId", eventId)
            .execute()
    }

    override fun getEventOrganizers(eventId: Int): List<Int> {
        return handle.createQuery(
            "select user_id from dbo.Roles where event_id = :eventId and name = 'Organizer'"
        )
            .bind("eventId", eventId)
            .mapTo(Int::class.java)
            .list()
    }
}