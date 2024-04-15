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
import org.springframework.web.bind.annotation.RestController
import project.planItAPI.http.PathTemplates
import project.planItAPI.services.EventsServices
import project.planItAPI.utils.EventInputModel
import project.planItAPI.utils.Failure
import project.planItAPI.utils.SearchEventInputModel
import project.planItAPI.utils.Success

/**
 * Controller class for handling event-related operations in a RESTful manner.
 *
 * @property eventsServices The service responsible for event-related business logic.
 */
@RestController
@RequestMapping(PathTemplates.PREFIX)
class EventsController(private val eventsServices: EventsServices) {

    @PostMapping(PathTemplates.CREATE_EVENT)
    fun createEvent(@RequestBody s: EventInputModel, @RequestAttribute("userId") userId: String): ResponseEntity<*> {
        return when (val res = eventsServices.createEvent(s.title, s.description, s.category, s.subcategory,
            s.location, s.visibility, s.date, s.endDate, s.price, userId.toInt())) {
            is Failure -> {
                    failureResponse(res)
            }

            is Success -> {
                return responseHandler(201, res.value)
            }
        }

    }

    @GetMapping(PathTemplates.GET_EVENT)
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

    @GetMapping(PathTemplates.USERS_IN_EVENT)
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

    @GetMapping(PathTemplates.SEARCH_EVENTS)
    fun searchEvents(@RequestBody filters: SearchEventInputModel): ResponseEntity<*>{
        return when (val res = eventsServices.searchEvents(filters.category, filters.subcategory, filters.price)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @PostMapping(PathTemplates.JOIN_EVENT)
    fun joinEvent() {
    }

    @PostMapping(PathTemplates.LEAVE_EVENT)
    fun leaveEvent(){}

    @DeleteMapping(PathTemplates.DELETE_EVENT)
    fun deleteEvent(){}

    @PutMapping(PathTemplates.EDIT_EVENT)
    fun editEvent(){}

}