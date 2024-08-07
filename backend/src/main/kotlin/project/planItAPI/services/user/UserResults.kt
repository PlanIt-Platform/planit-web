package project.planItAPI.services.user

import project.planItAPI.models.FeedbackOutputModel
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.models.RefreshTokensOutputModel
import project.planItAPI.models.RoleOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserEventsOutputModel
import project.planItAPI.models.UserInfo
import project.planItAPI.models.UserLogInOutputModel

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

/**
 * Type alias for the result of a user information retrieval operation.
 */
typealias GetUserResult = Either<Exception, UserInfo>

typealias GetUserEventsResult = Either<Exception, UserEventsOutputModel>

/**
 * Type alias for the result of a user information update operation.
 */
typealias EditUserResult = Either<Exception, SuccessMessage>

/**
 * Type alias for the result of a user profile picture upload operation.
 */
typealias UploadProfilePictureResult = Either<Exception, SuccessMessage>

/**
 * Type alias for the result of a user token retrieval operation.
 */
typealias UserTokensResult = Either<Exception, RefreshTokensOutputModel>

/**
 * Type alias for the result of a task creation operation.

 */
typealias AssignRoleResult = Either<Exception, RoleOutputModel>

/**
 * Type alias for the result of a task removal operation.
 */
typealias RemoveRoleResult = Either<Exception, SuccessMessage>

/**
 * Type alias for the result of a task
 */
typealias GetUserRoleResult = Either<Exception, RoleOutputModel>


/**
 * Type alias for the result of a feedback submission operation.
 */
typealias SendFeedbackResult = Either<Exception, SuccessMessage>

/**
 * Type alias for the result of a feedback retrieval operation.
 */
typealias GetFeedbackResult = Either<Exception, List<FeedbackOutputModel>>