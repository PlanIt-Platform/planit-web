package project.planItAPI.repository.jdbi.event

import org.jdbi.v3.core.Handle
import project.planItAPI.models.EventModel
import project.planItAPI.domain.event.Money
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
        subcategory: String?,
        location: String,
        visibility: String,
        date: Timestamp?,
        end_date: Timestamp?,
        price: Money?,
        userID: Int,
        password: String,
        code: String
    ): Int? {
        val eventId = handle.createUpdate(
            "insert into dbo.event(title, description, category, subcategory, location, visibility, date," +
                    " end_date, priceAmount, priceCurrency, password, code) values (:title, :description, :category," +
                    " :subcategory, :location, CAST(:visibility AS dbo.visibilitytype), :date, :end_date," +
                    " :priceAmount, :priceCurrency, :password, :code)"
        )
            .bind("title", title)
            .bind("description", description)
            .bind("category", category)
            .bind("subcategory", subcategory)
            .bind("location", location)
            .bind("visibility", visibility)
            .bind("date", date)
            .bind("end_date", end_date)
            .bind("priceAmount", price?.amount)
            .bind("priceCurrency", price?.currency)
            .bind("password", password)
            .bind("code", code)
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
        SELECT id, title, description, category, location, visibility, date
        FROM dbo.Event
        WHERE title LIKE :searchInput
        OR category LIKE :searchInput
        OR subcategory LIKE :searchInput
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

    override fun getAllEvents(limit: Int, offset: Int): SearchEventListOutputModel {
        return handle.createQuery(
            "select id, title, description, category, location, visibility, date " +
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

    override fun leaveEvent(userId: Int, eventId: Int) {
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
        subcategory: String?,
        location: String?,
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
                    "subcategory = :subcategory, " +
                    "location = :location, " +
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
            .bind("subcategory", subcategory)
            .bind("location", location)
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

    override fun kickUser(userId: Int, eventId: Int) {
        handle.createUpdate(
            "delete from dbo.UserParticipatesInEvent where user_id = :userId and event_id = :eventId"
        )
            .bind("userId", userId)
            .bind("eventId", eventId)
            .execute()
    }
}