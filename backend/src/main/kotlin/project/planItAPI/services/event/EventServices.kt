package project.planItAPI.services.event

import org.springframework.stereotype.Service
import project.planItAPI.domain.event.Category
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Description
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.domain.event.Title
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreateEventException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.models.SuccessMessage
import project.planItAPI.domain.event.Visibility
import project.planItAPI.domain.event.readCategories
import project.planItAPI.domain.event.readSubCategories
import project.planItAPI.models.EventOutputModel
import project.planItAPI.models.JoinEventWithCodeOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.services.generateEventCode
import project.planItAPI.services.getNowTime
import project.planItAPI.utils.CantKickYourselfException
import project.planItAPI.utils.EndDateBeforeDateException
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.OnlyOrganizerException
import project.planItAPI.utils.PastDateException
import project.planItAPI.utils.PrivateEventException
import project.planItAPI.utils.UserAlreadyInEventException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException
import java.sql.Timestamp

@Service
class EventServices(
    private val transactionManager: TransactionManager
) {

    /**
     * Creates a new event with the provided information.
     * @param title The title of the new event.
     * @param description The description of the new event.
     * @param category The category of the new event.
     * @param subcategory The subcategory of the new event.
     * @param location The location of the new event.
     * @param visibility The visibility of the new event.
     * @param date The date of the new event.
     * @param endDate The end date of the new event.
     * @param price The price of the new event.
     * @param userID The ID of the user creating the event.
     * @return [CreateEventOutputModel] containing the ID of the newly created event, its title and a status message.
     * If the creation fails, a [Failure] is thrown.
     */
    fun createEvent(
        title: Title,
        description: Description,
        category: Category,
        subcategory: Subcategory,
        location: String?,
        visibility: Visibility = Visibility.Public,
        date: DateFormat,
        endDate: DateFormat,
        price: Money,
        userID: Int,
        password: String
    ): CreateEventResult =
        transactionManager.run {
            val eventsRepository = it.eventsRepository
            if (visibility == Visibility.Private && password.isBlank()) throw PrivateEventException()
            val now = getNowTime()
            if (date.value < now) throw PastDateException()
            if (endDate.value != "" && endDate.value < date.value) throw EndDateBeforeDateException()
            val eventCode = generateEventCode()
            val eventID = eventsRepository.createEvent(
                title.value,
                description.value,
                category.name,
                subcategory.name,
                location ?: "To be Determined",
                visibility.name,
                Timestamp.valueOf("${date.value}:00"),
                if(endDate.value != "") Timestamp.valueOf("${endDate.value}:00") else null,
                price,
                userID,
                password,
                eventCode
            ) ?: throw FailedToCreateEventException()
            return@run CreateEventOutputModel(eventID, title.value, eventCode, "Created with success.")
        }


    /**
     * Retrieves the event associated with the given ID.
     * @param id The ID of the event to retrieve.
     * @return [EventResult] The event associated with the ID. If the event is not found, a [Failure] is thrown.
     */
    fun getEvent(id: Int, userID: Int): EventResult = transactionManager.run {
        val event = it.eventsRepository.getEvent(id) ?: throw EventNotFoundException()
        if(event.visibility != "Public") {
            val usersInEvent = it.eventsRepository.getUsersInEvent(id) ?: throw EventNotFoundException()
            val isUserInEvent = usersInEvent.users.any { user -> user.id == userID }
            if (!isUserInEvent) {
                throw UserNotInEventException()
            }
        }
        return@run EventOutputModel(event.id, event.title, event.description, event.category, event.subcategory,
            event.location, event.visibility, event.date, event.endDate, event.priceAmount, event.priceCurrency, event.code)
    }

    /**
     * Retrieves the users that are participating in the event with the given ID.
     * @param id The ID of the event to retrieve the users from.
     * @return [UsersInEventResult] The users participating in the event. If the event is not found, a [Failure] is thrown.
     */
    fun getUsersInEvent(id: Int, userId: Int): UsersInEventResult = transactionManager.run {
        val usersInEvent = it.eventsRepository.getUsersInEvent(id) ?: throw EventNotFoundException()
        val event = it.eventsRepository.getEvent(id) ?: throw EventNotFoundException()
        if(event.visibility == "Private"){
            val isUserInEvent = usersInEvent.users.any { user -> user.id == userId }
            if (!isUserInEvent) {
                throw UserNotInEventException()
            }
        }
        return@run usersInEvent
    }

    /**
     * Searches for events based on the provided user input.
     * @param searchInput The input to search for.
     * @return [SearchEventResult] The events that match the filters.
     */
    fun searchEvents(searchInput: String?, limit: Int, offset: Int): SearchEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository

        if (searchInput.isNullOrBlank() || searchInput == "All") {
            val eventList = eventsRepository.getAllEvents(limit, offset)
            return@run hidePrivateEventInfo(eventList)
        }

        val categories = readCategories()
        val subCategories = categories.keys.flatMap { cat -> readSubCategories(cat) ?: emptyList() }

        if (searchInput in categories.keys || searchInput in subCategories) {
            val eventList = eventsRepository.searchEvents(searchInput, limit, offset).events
                .filter { event -> event.visibility == "Public" }
            return@run hidePrivateEventInfo(SearchEventListOutputModel(eventList))
        }

        val eventList = eventsRepository.searchEvents(searchInput, limit, offset)
        return@run hidePrivateEventInfo(eventList)
    }

    /**
     * Allows a user to join an event.
     * @param userId The ID of the user joining the event.
     * @param eventId The ID of the event to join.
     * @param password The password of the event.
     * @return [JoinEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun joinEvent(userId: Int, eventId: Int, password: String): JoinEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
        if (event.password != password) throw IncorrectPasswordException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && usersInEvent.users.any { user -> user.id == userId }) {
            throw UserAlreadyInEventException()
        }
        eventsRepository.joinEvent(userId, event.id)
        return@run SuccessMessage("User joined event with success.")
    }

    /**
     * Allows a user to join an event through a code.
     * @param userId The ID of the user joining the event.
     * @param eventCode The code of the event to join.
     * @return [JoinEventWithCodeResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun joinEventByCode(userId: Int, eventCode: String): JoinEventWithCodeResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEventByCode(eventCode) ?: throw EventNotFoundException()
        if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && usersInEvent.users.any { user -> user.id == userId }) {
            throw UserAlreadyInEventException()
        }
        eventsRepository.joinEvent(userId, event.id)
        return@run JoinEventWithCodeOutputModel(event.title, event.id, "User joined event with success.")
    }

    /**
     * Allows a user to leave an event.
     * @param userId The ID of the user leaving the event.
     * @param eventId The ID of the event to leave.
     * @return [LeaveEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun leaveEvent(userId: Int, eventId: Int): LeaveEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) {
            throw UserNotInEventException()
        }
        val eventOrganizers = eventsRepository.getEventOrganizers(eventId)
        if (eventOrganizers.size == 1 && eventOrganizers.contains(userId)) {
            throw OnlyOrganizerException()
        }
        eventsRepository.leaveEvent(userId, event.id)
        return@run SuccessMessage("User left event with success.")
    }

    /**
     * Deletes the event with the given ID.
     * @param userId The ID of the user deleting the event.
     * @param eventId The ID of the event to delete.
     * @return [DeleteEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun deleteEvent(userId: Int, eventId: Int): DeleteEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (userId !in eventsRepository.getEventOrganizers(eventId)) { throw UserIsNotOrganizerException() }
        eventsRepository.deleteEvent(event.id)
        return@run SuccessMessage("Event deleted with success.")
    }

    /**
     * Edits the event with the given ID.
     * @param userId The ID of the user editing the event.
     * @param eventId The ID of the event to edit.
     * @param title The new title of the event.
     * @param description The new description of the event.
     * @param category The new category of the event.
     * @param subcategory The new subcategory of the event.
     * @param location The new location of the event.
     * @param visibility The new visibility of the event.
     * @param date The new date of the event.
     * @param endDate The new end date of the event.
     * @param price The new price of the event.
     * @return [EditEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun editEvent(
        userId: Int,
        eventId: Int,
        title: Title,
        description: Description,
        category: Category,
        subcategory: Subcategory,
        location: String?,
        visibility: Visibility,
        date: DateFormat,
        endDate: DateFormat,
        price: Money,
        password: String
    ): EditEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        if (visibility == Visibility.Private && password.isBlank()) throw PrivateEventException()
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
        if (userId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
        val now = getNowTime()
        if (date.value < now) throw PastDateException()
        if (endDate.value != "" && endDate.value < date.value) throw EndDateBeforeDateException()
        val newPassword = if (visibility == Visibility.Private && password.isBlank()) event.password
        else if (visibility == Visibility.Public) ""
        else password
        eventsRepository.editEvent(
            eventId,
            title.value,
            description.value,
            category.name,
            subcategory.name,
            location ?: "To be Determined",
            visibility.name,
            Timestamp.valueOf("${date.value}:00"),
            if(endDate.value != "") Timestamp.valueOf("${endDate.value}:00") else null,
            price,
            newPassword
        )
        return@run SuccessMessage("Event edited with success.")
    }

    /**
     * Retrieves the list of event categories.
     * @return [CategoriesResult] The list of event categories.
     * If the categories are not found, a [Failure] is thrown.
     */
    fun getCategories(): CategoriesResult = transactionManager.run {
        return@run readCategories().keys.toList()
    }

    /**
     * Retrieves the list of event subcategories for the given category.
     * @param category The category to retrieve the subcategories from.
     * @return [SubcategoriesResult] The list of event subcategories for the given category.
     */
    fun getSubcategories(category: String): SubcategoriesResult = transactionManager.run {
        return@run readSubCategories(category) ?: throw InvalidValueException("category")
    }

    /**
     * Kicks a user from the event.
     * @param organizerId The ID of the user kicking the other user.
     * @param eventId The ID of the event to kick the user from.
     * @param userId The ID of the user to kick from the event.
     * @return [KickUserResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun kickUser(organizerId: Int, eventId: Int, userId: Int): KickUserResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (organizerId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) {
            throw UserNotInEventException()
        }
        if (organizerId == userId) throw CantKickYourselfException()
        eventsRepository.kickUser(userId, event.id)
        return@run SuccessMessage("User kicked from event with success.")
    }

    private fun hidePrivateEventInfo(events: SearchEventListOutputModel): SearchEventListOutputModel {
        val eventsReturn = SearchEventListOutputModel(events.events.map { event ->
            if (event.visibility == "Private") {
                event.copy(description = null, location = null, date = null, category = null)
            } else {
                event
            }
        })
        return eventsReturn
    }
}