package project.planItAPI.repository.jdbi.events

import org.jdbi.v3.core.Handle
import project.planItAPI.utils.EventOutputModel
import project.planItAPI.utils.Money
import project.planItAPI.utils.SearchEventOutputModel
import project.planItAPI.utils.UserInEvent
import project.planItAPI.utils.UsersInEventList
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
        userID: Int
    ): Int? {
        return handle.inTransaction<Int?, Exception> { handle ->
            val eventId = handle.createUpdate(
                "insert into dbo.event(title, description, category, subcategory, location, visibility, date, end_date, price) " +
                        "values (:title, :description, :category, :subcategory, :location, CAST(:visibility AS dbo.visibilitytype), :date, :end_date, :price)"
            )
                .bind("title", title)
                .bind("description", description)
                .bind("category", category)
                .bind("subcategory", subcategory)
                .bind("location", location)
                .bind("visibility", visibility)
                .bind("date", date)
                .bind("end_date", end_date)
                .bind("price", price)
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

            eventId
        }
    }

    override fun getEvent(id: Int): EventOutputModel? {
        return handle.createQuery(
            "select * from dbo.Event where id = :id"
        )
            .bind("id", id)
            .mapTo(EventOutputModel::class.java)
            .singleOrNull()
    }

    override fun getUsersInEvent(id: Int): UsersInEventList? {
        return handle.createQuery(
            """
        SELECT u.id, u.name, u.username
        FROM dbo.UserParticipatesInEvent up
        JOIN dbo.Users u ON up.user_id = u.id
        WHERE up.event_id = :id
        """
        )
            .bind("id", id)
            .mapTo(UserInEvent::class.java)
            .list()
            .let { UsersInEventList(it) }
    }

    override fun searchEvents(category: String?, subcategory: String?, price: Money?): SearchEventOutputModel {
        TODO("Not yet implemented")
    }

}