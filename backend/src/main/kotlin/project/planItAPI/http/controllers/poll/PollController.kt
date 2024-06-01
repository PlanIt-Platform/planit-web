package project.planItAPI.http.controllers.poll

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
import project.planItAPI.domain.Id
import project.planItAPI.http.PathTemplates
import project.planItAPI.http.PathTemplates.GET_POLLS
import project.planItAPI.http.controllers.failureResponse
import project.planItAPI.http.controllers.responseHandler
import project.planItAPI.models.PollInputModel
import project.planItAPI.services.poll.PollServices
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success

@RestController
@RequestMapping(PathTemplates.PREFIX)
class PollController(private val pollServices: PollServices) {

    @PostMapping(PathTemplates.CREATE_POLL)
    fun createPoll(
        @RequestAttribute("userId") userId: String,
        @RequestBody input: PollInputModel,
        @PathVariable eventId: Int
    ): ResponseEntity<*> {
        val idResult = Id(eventId)
        if (idResult is Failure) return failureResponse(idResult)
        return when (val validation = validatePollInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (val res = pollServices.createPoll(
                    validatedInputs.title,
                    validatedInputs.options,
                    validatedInputs.duration,
                    userId.toInt(),
                    eventId
                )) {
                    is Failure -> failureResponse(res)
                    is Success -> responseHandler(201, res.value)
                }
            }
        }
    }

    @GetMapping(PathTemplates.POLL)
    fun getPoll(@PathVariable pollId: Int, @PathVariable eventId: Int): ResponseEntity<*> {
        return when (val eventIdResult = Id(eventId)) {
            is Failure -> failureResponse(eventIdResult)
            is Success -> {
                when (val idResult = Id(pollId)) {
                    is Failure -> failureResponse(idResult)
                    is Success -> {
                        when (val res = pollServices.getPoll(pollId, eventId)) {
                            is Failure -> failureResponse(res)
                            is Success -> {
                                return responseHandler(200, res.value)
                            }
                        }
                    }
                }
            }
        }
    }

    @DeleteMapping(PathTemplates.POLL)
    fun deletePoll(
        @RequestAttribute("userId") userId: String,
        @PathVariable pollId: Int,
        @PathVariable eventId: Int
    ): ResponseEntity<*> {
        return when (val eventIdResult = Id(eventId)) {
            is Failure -> failureResponse(eventIdResult)
            is Success -> {
                when (val idResult = Id(pollId)) {
                    is Failure -> failureResponse(idResult)
                    is Success -> {
                        when (val res = pollServices.deletePoll(userId.toInt(), pollId, eventId)) {
                            is Failure -> failureResponse(res)
                            is Success -> {
                                return responseHandler(200, res.value)
                            }
                        }
                    }
                }
            }
        }
    }

    @PutMapping(PathTemplates.VOTE_POLL)
    fun votePoll(
        @RequestAttribute("userId") userId: String,
        @PathVariable pollId: Int,
        @PathVariable optionId: Int,
        @PathVariable eventId: Int
    ): ResponseEntity<*> {
        return when (val eventIdResult = Id(eventId)) {
            is Failure -> failureResponse(eventIdResult)
            is Success -> {
                when (val pollIdResult = Id(pollId)) {
                    is Failure -> failureResponse(pollIdResult)
                    is Success -> {
                        when (val optionIdResult = Id(optionId)) {
                            is Failure -> failureResponse(optionIdResult)
                            is Success -> {
                                when (val res = pollServices.votePoll(userId.toInt(), pollId, optionId, eventId)) {
                                    is Failure -> failureResponse(res)
                                    is Success -> {
                                        return responseHandler(200, res.value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @GetMapping(GET_POLLS)
    fun getPolls(@PathVariable eventId: Int): ResponseEntity<*> {
        return when (val eventIdResult = Id(eventId)) {
            is Failure -> failureResponse(eventIdResult)
            is Success -> {
                when (val res = pollServices.getPolls(eventId)) {
                    is Failure -> failureResponse(res)
                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }
}
