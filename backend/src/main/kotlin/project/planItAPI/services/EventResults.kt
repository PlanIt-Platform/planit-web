package project.planItAPI.services

import project.planItAPI.utils.CreateEventOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.EventOutputModel
import project.planItAPI.utils.UsersInEventList

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