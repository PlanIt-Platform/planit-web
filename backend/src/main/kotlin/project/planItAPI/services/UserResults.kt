package project.planItAPI.services

import project.planItAPI.utils.UserRegisterOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.UserLogInOutputModel


/**
 * Type alias for the result of a user registration operation.
 */
typealias UserRegisterResult = Either<Exception, UserRegisterOutputModel>

/**
 * Type alias for the result of a user login operation.
 */
typealias UserLoginResult = Either<Exception, UserLogInOutputModel>

/**
 * Type alias for the result of a user logout operation.
 */
typealias LogoutResult = Either<Exception, Unit>
