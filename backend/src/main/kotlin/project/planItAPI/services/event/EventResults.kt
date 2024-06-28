package project.planItAPI.services.event

import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.models.EventModel
import project.planItAPI.models.EventOutputModel
import project.planItAPI.models.JoinEventWithCodeOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UsersInEventList

/**
 * Represents the result of creating an event.
 */
typealias CreateEventResult = Either<Exception, CreateEventOutputModel>

/**
 * Represents the result of retrieving an event.
 */
typealias EventResult = Either<Exception, EventOutputModel>

/**
 * Represents the result of retrieving a list of users in an event.
 */
typealias UsersInEventResult = Either<Exception, UsersInEventList>

/**
 * Represents the result of searching for an event.
 */
typealias SearchEventResult = Either<Exception, SearchEventListOutputModel>

/**
 * Represents the result of joining an event.
 */
typealias JoinEventResult = Either<Exception, SuccessMessage>

/**
 * Represents the result of joining an event with a code.
 */
typealias JoinEventWithCodeResult = Either<Exception, JoinEventWithCodeOutputModel>

/**
 * Represents the result of leaving an event.
 */

typealias LeaveEventResult = Either<Exception, SuccessMessage>

/**
 * Represents the result of deleting an event.
 */
typealias DeleteEventResult = Either<Exception, SuccessMessage>

/**
 * Represents the result of editing an event.
 */
typealias EditEventResult = Either<Exception, SuccessMessage>

/**
 * Represents the result of retrieving a list of event categories.
 */
typealias CategoriesResult = Either<Exception, List<String>>

/**
 * Represents the result of retrieving a list of event subcategories.
 */
typealias SubcategoriesResult = Either<Exception, List<String>>

/**
 * Represents the result of kicking a user from an event.
 */
typealias KickUserResult = Either<Exception, SuccessMessage>
