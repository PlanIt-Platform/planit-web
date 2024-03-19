package project.planItAPI.http.model.user

/**
 * Input model for user registration.
 *
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property password The password chosen by the user.
 */
data class UserRegisterInputModel(
    val name: String,
    val email: String,
    val password: String,
)
