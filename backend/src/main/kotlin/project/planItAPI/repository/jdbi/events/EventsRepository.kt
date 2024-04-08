package project.planItAPI.repository.jdbi.events

import org.springframework.stereotype.Component
import project.planItAPI.utils.EventOutputModel
import project.planItAPI.utils.Money
import project.planItAPI.utils.UsersInEventList
import java.sql.Timestamp

/**
 * Repository interface for managing event-related data.
 */
@Component
interface EventsRepository {

    /**
     * Creates a new event with the provided information.
     *
     * @param title The title of the new event.
     * @param description The description of the new event.
     * @param category The category of the new event.
     * @param subcategory The subcategory of the new event.
     * @param location The location of the new event.
     * @param visibility The visibility of the new event.
     * @param date The date of the new event.
     * @param end_date The end date of the new event.
     * @param price The price of the new event.
     * @param userID The ID of the user creating the event.
     * @return The ID of the newly created event, or null if creation fails.
     */
    fun createEvent(
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
    ): Int?

    /**
     * Retrieves the event associated with the given ID.
     *
     * @param id The ID of the event to retrieve.
     * @return The event associated with the ID, or null if not found.
     */
    fun getEvent(id: Int): EventOutputModel?

    /**
     * Retrieves the users participating in the event associated with the given ID.
     *
     * @param id The ID of the event to retrieve the users for.
     * @return [UsersInEventList] containing the users participating in the event, or null if not found.
     */
    fun getUsersInEvent(id: Int): UsersInEventList?
}