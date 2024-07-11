package project.planItAPI.repository.jdbi.event

import org.springframework.stereotype.Component
import project.planItAPI.domain.event.Coordinates
import project.planItAPI.models.EventModel
import project.planItAPI.domain.event.Money
import project.planItAPI.models.FindNearbyEventsOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.models.UsersInEventList
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
     * @param locationType the type of the location.
     * @param location The location of the new event.
     * @param visibility The visibility of the new event.
     * @param date The date of the new event.
     * @param end_date The end date of the new event.
     * @param price The price of the new event.
     * @param userID The ID of the user creating the event.
     * @param password The password of the event.
     * @param eventCode The event code of the event.
     * @return The ID of the newly created event, or null if creation fails.
     */
    fun createEvent(
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
    ): Int?

    /**
     * Retrieves the event associated with the given ID.
     *
     * @param id The ID of the event to retrieve.
     * @return The event associated with the ID, or null if not found.
     */
    fun getEvent(id: Int): EventModel?

    /**
     * Retrieves the event associated with the given event code.
     *
     * @param eventCode The event code of the event to retrieve.
     * @return The event associated with the event code, or null if not found.
     */
    fun getEventByCode(eventCode: String): EventModel?

    /**
     * Retrieves the users participating in the event associated with the given ID.
     *
     * @param id The ID of the event to retrieve the users for.
     * @return [UsersInEventList] containing the users participating in the event, or null if not found.
     */
    fun getUsersInEvent(id: Int): UsersInEventList?

    /**
     * Retrieves the events that match the given search criteria.
     * @param searchInput The search criteria to match events against.
     * @return A list of [EventModel] containing the events that match the search criteria.
     */
    fun searchEvents(searchInput: String, limit: Int, offset: Int): SearchEventListOutputModel

    /**
     * Retrieves the events that match the given category.
     * @param category The category to match events against.
     * @return A list of [EventModel] containing the events that match the category.
     */
    fun searchEventsByCategory(category: String, limit: Int, offset: Int): SearchEventListOutputModel

    /**
     * Retrieves the events that are nearby the user with the given coordinates.
     * @param userCoords The coordinates of the user.
     * @param radius The radius in which to search for events.
     * @param limit The maximum number of events to return.
     * @param userId The ID of the user.
     * @return A list of [FindNearbyEventsOutputModel] containing the nearby events.
     */
    fun getNearbyEvents(userCoords: Coordinates, radius: Int, limit: Int, userId: Int): List<FindNearbyEventsOutputModel>

    /**
     * Retrieves all events.
     * @return A list of [EventModel] containing all events.
     */
    fun getAllEvents(limit: Int, offset: Int): SearchEventListOutputModel

    /**
     * Adds a user to the event with the given ID.
     * @param userId The ID of the user to add to the event.
     * @param eventId The ID of the event to add the user to.
     */
    fun joinEvent(userId: Int, eventId: Int)

    /**
     * Kicks a user from the event with the given ID.
     * @param eventId The ID of the event to kick the user from.
     * @param userId The ID of the user to kick from the event.
     */
    fun kickUserFromEvent(userId: Int, eventId: Int)

    /**
     * Deletes the event with the given ID.
     * @param eventId The ID of the event to delete.
     */
    fun deleteEvent(eventId: Int)

    /**
     * Edits the event with the given ID.
     * @param eventId The ID of the event to edit.
     */
    fun editEvent(
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
    )

    /**
     * Retrieves the ID of the user that organized the event with the given ID.
     * @param eventId The ID of the event to retrieve the organizer for.
     * @return The IDs of the users that organized the event.
     */
    fun getEventOrganizers(eventId: Int): List<Int>
}