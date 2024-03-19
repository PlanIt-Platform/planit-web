package project.planItAPI.services

import project.planItAPI.utils.Either


/**
 * Type alias for the result of a user registration operation.
 */
typealias UserRegisterResult = Either<Exception, UserRegisterOutputModel>