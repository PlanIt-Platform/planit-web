package project.planItAPI.http.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import project.planItAPI.http.PathTemplates.CREATE_EVENT
import project.planItAPI.http.PathTemplates.DELETE_EVENT
import project.planItAPI.http.PathTemplates.EDIT_EVENT
import project.planItAPI.http.PathTemplates.GET_EVENT
import project.planItAPI.http.PathTemplates.JOIN_EVENT
import project.planItAPI.http.PathTemplates.LEAVE_EVENT
import project.planItAPI.http.PathTemplates.PREFIX
import project.planItAPI.http.PathTemplates.SEARCH_EVENTS
import project.planItAPI.http.PathTemplates.USERS_IN_EVENT
import project.planItAPI.services.EventsServices
import project.planItAPI.utils.EventInputModel
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success

/**
 * Controller class for handling event-related operations in a RESTful manner.
 *
 * @property eventsServices The service responsible for event-related business logic.
 */
@RestController
@RequestMapping(PREFIX)
class EventsController(private val eventsServices: EventsServices) {

    @PostMapping(CREATE_EVENT)
    fun createEvent(@RequestBody s: EventInputModel, @RequestAttribute("userId") userId: String): ResponseEntity<*> {
        return when (val res = eventsServices.createEvent(s.title, s.description, s.category, s.subCategory,
            s.location, s.visibility, s.date, s.endDate, s.price, userId.toInt())) {
            is Failure -> {
                    failureResponse(res)
            }

            is Success -> {
                return responseHandler(201, res.value)
            }
        }

    }

    @GetMapping(GET_EVENT)
    fun getEvent(@PathVariable id: Int): ResponseEntity<*> {
        return when (val res = eventsServices.getEvent(id)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @GetMapping(USERS_IN_EVENT)
    fun getUsersInEvent(@PathVariable id: Int): ResponseEntity<*> {
        return when (val res = eventsServices.getUsersInEvent(id)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @GetMapping(SEARCH_EVENTS)
    fun searchEvents(@RequestParam searchInput: String): ResponseEntity<*> {
        return when (val res = eventsServices.searchEvents(searchInput)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @PostMapping(JOIN_EVENT)
    fun joinEvent(@RequestAttribute("userId") userId: String, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val res = eventsServices.joinEvent(userId.toInt(), eventId)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @PostMapping(LEAVE_EVENT)
    fun leaveEvent(@RequestAttribute("userId") userId: String, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val res = eventsServices.leaveEvent(userId.toInt(), eventId)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @DeleteMapping(DELETE_EVENT)
    fun deleteEvent(@RequestAttribute("userId") userId: String, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val res = eventsServices.deleteEvent(userId.toInt(), eventId)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @PutMapping(EDIT_EVENT)
    fun editEvent(
        @PathVariable eventId: Int,
        @RequestAttribute("userId") userId: String,
        @RequestBody input: EventInputModel): ResponseEntity<*>{
        return when (
            val res = eventsServices.editEvent(
                userId.toInt(),
                eventId,
                input.title,
                input.description,
                input.category,
                input.subCategory,
                input.location,
                input.visibility,
                input.date,
                input.endDate,
                input.price
            )
        ) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }
}