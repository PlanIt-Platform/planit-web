package project.planItAPI.utils


// MESSAGE CONSTANTS
private const val PASSWORD_TOO_SHORT = "Password must be at least 5 characters long"
private const val PASSWORD_NO_NUMBER = "Password must contain at least one number"
private const val PASSWORD_NO_UPPERCASE = "Password must contain at least one uppercase letter"
private const val PASSWORD_NO_SPECIAL_CHAR = "Password must contain at least one special character"
private const val MULTIPLE_PASSWORD_MESSAGE = "Password must follow the following parameters: "
private const val EXISTING_EMAIL = "Email is already being used."
private const val EXISTING_USERNAME = "Username is already being used."
private const val USER_REGISTER_ERROR_MESSAGE = "There was an error registering the user."
private const val INCORRECT_LOGIN_MESSAGE = "Email or username not found."
private const val USER_NOT_FOUND_MESSAGE = "User not found."
private const val UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported media type."

// HTTP CODE CONSTANTS
private const val BAD_REQUEST = 400
private const val NOT_FOUND = 404
private const val UNSUPPORTED_MEDIA_TYPE = 451
private const val INTERNAL_SERVER_ERROR = 500

/**
 * Base class for exceptions that return an HTTP code.
 * @param message The message to be returned.
 * @param httpCode The HTTP code to be returned.
 */
open class HTTPCodeException(override val message: String, val httpCode: Int) : Exception()

/**
 * Base class for exceptions that indicate that the password is not safe.
 * @param message The detail message of the exception.
 */
open class PasswordException(override val message: String) : Exception()

/**
 * Exception indicating that the password is too short.
 */
class PasswordTooShort : PasswordException (message = PASSWORD_TOO_SHORT)

/**
 * Exception indicating that the password has no number.
 */
class PasswordHasNoNumber : PasswordException (message = PASSWORD_NO_NUMBER)

/**
 * Exception indicating that the password has no uppercase letter.
 */
class PasswordHasNoUppercase : PasswordException (message = PASSWORD_NO_UPPERCASE)

/**
 * Exception indicating that the password has no special character.
 */
class PasswordHasNoSpecialChar : PasswordException (message = PASSWORD_NO_SPECIAL_CHAR)

/**
 * Exception indicating that the password has multiple problems.
 * All the password exceptions are a bad request, so the HTTP code is 400.
 * @param exceptions The list of exceptions that the password has.
 */
class MultiplePasswordExceptions (
    private val exceptions: List<PasswordException>
) : HTTPCodeException(
    message = MULTIPLE_PASSWORD_MESSAGE + exceptions.joinToString(", ") { it.message },
    httpCode = BAD_REQUEST
)

/**
 * Exception indicating that the email is already being used.
 * The email is unique, so the HTTP code is 400, because it is a bad request.
 */
class ExistingEmailException : HTTPCodeException(message = EXISTING_EMAIL, httpCode = BAD_REQUEST)

/**
 * Exception indicating that the username is already being used.
 * The username is unique, so the HTTP code is 400, because it is a bad request.
 */
class ExistingUsernameException : HTTPCodeException(message = EXISTING_USERNAME, httpCode = BAD_REQUEST)

/**
 * Exception indicating that there was an error registering the user.
 * The HTTP code is 500, because it is an internal server error.
 */
class UserRegisterErrorException : HTTPCodeException(
    message = USER_REGISTER_ERROR_MESSAGE,
    httpCode = INTERNAL_SERVER_ERROR
)

/**
 * Exception indicating that the password is incorrect.
 * The HTTP code is 400, because it is a bad request.
 */
class IncorrectPasswordException : HTTPCodeException(
    message = "Incorrect password",
    httpCode = BAD_REQUEST
)

/**
 * Exception indicating that the event is private.
 * The HTTP code is 400, because it is a bad request.
 */
class PrivateEventException : HTTPCodeException(
    message = "Event is private. Password is required",
    httpCode = BAD_REQUEST
)

/**
 * Exception indicating that the login is incorrect.
 * The HTTP code is 400, because it is a bad request.
 */
class IncorrectLoginException : HTTPCodeException(
    message = INCORRECT_LOGIN_MESSAGE,
    httpCode = BAD_REQUEST
)

/**
 * Exception indicating that the user was not found.
 * The HTTP code is 404, because it was not found.
 */
class UserNotFoundException : HTTPCodeException(
    message = USER_NOT_FOUND_MESSAGE,
    httpCode = NOT_FOUND
)

/**
 * Exception indicating that the media type is not supported.
 * The HTTP code is 415, because it is an unsupported media type.
 */
class UnsupportedMediaTypeException : HTTPCodeException(
    message = UNSUPPORTED_MEDIA_TYPE_MESSAGE,
    httpCode = UNSUPPORTED_MEDIA_TYPE
)

class InvalidTimestampFormatException (name: String) : HTTPCodeException(
        message = "$name has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'",
    httpCode = BAD_REQUEST
)

class InvalidPriceFormatException : HTTPCodeException(
    message = "Invalid price format. The correct format is 'amount currency'. Example: '10.00 USD'",
    httpCode = BAD_REQUEST
)

class InvalidSubcategoryException : HTTPCodeException(
    message = "Invalid subcategory",
    httpCode = BAD_REQUEST
)

class EventNotFoundException : HTTPCodeException(
    message = "Event not found",
    httpCode = NOT_FOUND
)

class UserNotInEventException : HTTPCodeException(
    message = "User is not in the event",
    httpCode = BAD_REQUEST
)

class UserAlreadyInEventException : HTTPCodeException(
    message = "User is already in the event",
    httpCode = BAD_REQUEST
)

class FailedToCreateEventException : HTTPCodeException(
    message = "Failed to create event",
    httpCode = BAD_REQUEST
)

class EventHasEndedException : HTTPCodeException(
    message = "Event has ended",
    httpCode = BAD_REQUEST
)

class PastDateException : HTTPCodeException(
    message = "Date not available, must be later than the current date",
    httpCode = BAD_REQUEST
)

class EndDateBeforeDateException : HTTPCodeException(
    message = "End date must be later than the date chosen",
    httpCode = BAD_REQUEST
)

class UserIsNotOrganizerException : HTTPCodeException(
    message = "User is not the organizer of the event",
    httpCode = BAD_REQUEST
)

class OnlyOrganizerException : HTTPCodeException(
    message = "User is the only organizer of the event",
    httpCode = BAD_REQUEST
)

class InvalidValueException (value: String): HTTPCodeException(
    message = "Invalid $value",
    httpCode = BAD_REQUEST
)

class InvalidValueLengthException (value: String) : HTTPCodeException(
    message = "Invalid $value length",
    httpCode = BAD_REQUEST
)

class ValueIsBlankException (value: String) : HTTPCodeException(
    message = "$value is blank",
    httpCode = BAD_REQUEST
)

class InvalidDurationException : HTTPCodeException(
    message = "Invalid duration, must be one of the following values: 1 hour, 4 hours, 8 hours, 12 hours, 24 hours or 72 hours",
    httpCode = BAD_REQUEST
)

class InvalidNumberOfOptionsException : HTTPCodeException(
    message = "Invalid number of options, must be between 2 and 5",
    httpCode = BAD_REQUEST
)

class FailedToCreatePollException : HTTPCodeException(
    message = "Failed to create poll",
    httpCode = BAD_REQUEST
)

class PollNotFoundException : HTTPCodeException(
    message = "Poll not found",
    httpCode = NOT_FOUND
)

class PollHasEndedException : HTTPCodeException(
    message = "Poll has ended",
    httpCode = BAD_REQUEST
)

class OptionNotFoundException : HTTPCodeException(
    message = "Option not found",
    httpCode = NOT_FOUND
)

class UserAlreadyVotedException : HTTPCodeException(
    message = "You have already voted",
    httpCode = BAD_REQUEST
)

class InterestsAreDuplicatedException : HTTPCodeException(
    message = "Interests cannot contain duplicate categories",
    httpCode = BAD_REQUEST
)

class InvalidRefreshTokenException : HTTPCodeException(
    message = "Invalid refresh token",
    httpCode = BAD_REQUEST
)

class FailedToAssignRoleException : HTTPCodeException(
    message = "Failed to assign role",
    httpCode = BAD_REQUEST
)

class RoleNotFoundException : HTTPCodeException(
    message = "Role not found",
    httpCode = NOT_FOUND
)

class CantKickYourselfException : HTTPCodeException(
    message = "You can't kick yourself out of the event",
    httpCode = BAD_REQUEST
)

class InvalidLimitAndOffsetException (limit: Boolean, offset: Boolean): HTTPCodeException(
    message = (if(limit && offset)"Invalid limit and offset." else if (limit) "Invalid limit." else "Invalid offset."),
    httpCode = BAD_REQUEST
){
    init {
        if (!limit && !offset) {
            throw IllegalArgumentException("At least one of 'limit' or 'offset' must be true")
        }
    }
}

class InvalidEventCodeException : HTTPCodeException(
    message = "Invalid event code",
    httpCode = BAD_REQUEST
)

class MustSpecifyLocationTypeException : HTTPCodeException(
    message = "If location is specified, then locationType must be specified as well",
    httpCode = BAD_REQUEST
)