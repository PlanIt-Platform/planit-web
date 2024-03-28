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
    val exceptions: List<PasswordException>
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
    message = "Incorrect password.",
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
