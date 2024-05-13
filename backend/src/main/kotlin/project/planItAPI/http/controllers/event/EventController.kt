package project.planItAPI.http.controllers.event

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
import project.planItAPI.domain.event.Category
import project.planItAPI.domain.Id
import project.planItAPI.http.PathTemplates.CATEGORIES
import project.planItAPI.http.PathTemplates.CREATE_EVENT
import project.planItAPI.http.PathTemplates.DELETE_EVENT
import project.planItAPI.http.PathTemplates.EDIT_EVENT
import project.planItAPI.http.PathTemplates.GET_EVENT
import project.planItAPI.http.PathTemplates.JOIN_EVENT
import project.planItAPI.http.PathTemplates.LEAVE_EVENT
import project.planItAPI.http.PathTemplates.PREFIX
import project.planItAPI.http.PathTemplates.SEARCH_EVENTS
import project.planItAPI.http.PathTemplates.SUBCATEGORIES
import project.planItAPI.http.PathTemplates.USERS_IN_EVENT
import project.planItAPI.http.controllers.failureResponse
import project.planItAPI.http.controllers.responseHandler
import project.planItAPI.services.event.EventServices
import project.planItAPI.models.EventInputModel
import project.planItAPI.models.EventPasswordModel
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success

/**
 * Controller class for handling event-related operations in a RESTful manner.
 *
 * @property eventServices The service responsible for event-related business logic.
 */
@RestController
@RequestMapping(PREFIX)
class EventController(private val eventServices: EventServices) {

    @PostMapping(CREATE_EVENT)
    fun createEvent(@RequestBody input: EventInputModel, @RequestAttribute("userId") userId: String): ResponseEntity<*> {
        return when (val validation = validateEventInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (val res = eventServices.createEvent(
                    input.title,
                    input.description,
                    validatedInputs.category,
                    validatedInputs.subCategory,
                    input.location,
                    validatedInputs.visibility,
                    validatedInputs.date,
                    validatedInputs.endDate,
                    validatedInputs.price,
                    userId.toInt(),
                    input.password
                )) {
                    is Failure -> failureResponse(res)
                    is Success -> responseHandler(201, res.value)
                }
            }
        }
    }

    @GetMapping(GET_EVENT)
    fun getEvent(@PathVariable id: Int): ResponseEntity<*> {
        return when (val idResult = Id(id)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                when (val res = eventServices.getEvent(id)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    @GetMapping(USERS_IN_EVENT)
    fun getUsersInEvent(@PathVariable id: Int): ResponseEntity<*> {
        return when (val idResult = Id(id)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                when (val res = eventServices.getUsersInEvent(id)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    @GetMapping(SEARCH_EVENTS)
    fun searchEvents(@RequestParam searchInput: String): ResponseEntity<*> {
        return when (val res = eventServices.searchEvents(searchInput)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @PostMapping(JOIN_EVENT)
    fun joinEvent(
        @RequestAttribute("userId") userId: String,
        @PathVariable eventId: Int,
        @RequestBody eventPw: EventPasswordModel
    ): ResponseEntity<*> {
        return when(val idResult = Id(eventId)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                return when (val res = eventServices.joinEvent(userId.toInt(), eventId, eventPw.password)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    @PostMapping(LEAVE_EVENT)
    fun leaveEvent(@RequestAttribute("userId") userId: String, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val idResult = Id(eventId)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                when (val res = eventServices.leaveEvent(userId.toInt(), eventId)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    @DeleteMapping(DELETE_EVENT)
    fun deleteEvent(@RequestAttribute("userId") userId: String, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val idResult = Id(eventId)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                when (val res = eventServices.deleteEvent(userId.toInt(), eventId)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    @PutMapping(EDIT_EVENT)
    fun editEvent(
        @PathVariable eventId: Int,
        @RequestAttribute("userId") userId: String,
        @RequestBody input: EventInputModel
    ): ResponseEntity<*>{
        val idResult = Id(eventId)
        if (idResult is Failure) {
            return failureResponse(idResult)
        }
        return when (val validation = validateEventInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (val res = eventServices.editEvent(
                    userId.toInt(),
                    eventId,
                    input.title,
                    input.description,
                    validatedInputs.category,
                    validatedInputs.subCategory,
                    input.location,
                    validatedInputs.visibility,
                    validatedInputs.date,
                    validatedInputs.endDate,
                    validatedInputs.price
                )) {
                    is Failure -> failureResponse(res)
                    is Success -> responseHandler(200, res.value)
                }
            }
        }
    }

    @GetMapping(CATEGORIES)
    fun getCategories(): ResponseEntity<*> {
        return when (val res = eventServices.getCategories()) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    @GetMapping(SUBCATEGORIES)
    fun getSubcategories(@PathVariable category: String): ResponseEntity<*> {
        return when (val categoryResult = Category(category)) {
            is Failure -> failureResponse(categoryResult)
            is Success -> {
                when (val res = eventServices.getSubcategories(category)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        if (res.value.isEmpty()) {
                            return responseHandler(200, "No subcategories found for category $category.")
                        }
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }
}
