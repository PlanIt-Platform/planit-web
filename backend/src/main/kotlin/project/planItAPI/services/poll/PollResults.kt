package project.planItAPI.services.poll

import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.models.CreatePollOutputModel
import project.planItAPI.models.PollOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.utils.Either

/**
 * The result of creating a poll.

 */
typealias CreatePollResult = Either<Exception, CreatePollOutputModel>

/**
 * The result of getting a poll.
 */
typealias GetPollResult = Either<Exception, PollOutputModel>

/**
 * The result of deleting a poll.
 */
typealias DeletePollResult = Either<Exception, SuccessMessage>

/**
 * The result of voting in a poll.
 */
typealias VoteResult = Either<Exception, SuccessMessage>