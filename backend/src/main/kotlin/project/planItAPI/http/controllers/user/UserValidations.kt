package project.planItAPI.http.controllers.user

import project.planItAPI.domain.event.Category
import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrUsername
import project.planItAPI.domain.user.Password
import project.planItAPI.domain.user.Name
import project.planItAPI.domain.user.Username
import project.planItAPI.models.UserEditModel
import project.planItAPI.models.UserLoginInputModel
import project.planItAPI.models.UserRegisterInputModel
import project.planItAPI.models.ValidatedUserEditInputsModel
import project.planItAPI.models.ValidatedUserLoginInputsModel
import project.planItAPI.models.ValidatedUserRegisterInputsModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InterestsAreDuplicatedException
import project.planItAPI.utils.Success

fun validateUserRegisterInput(input: UserRegisterInputModel): Either<Exception, ValidatedUserRegisterInputsModel> {
    val results = listOf(
        Username(input.username),
        Name(input.name),
        Email(input.email),
        Password(input.password)
    )

    val errors = results.mapNotNull {
        when (it) {
            is Failure -> it.value
            else -> null
        }
    }

    return if (errors.isEmpty()) {
        Success(
            ValidatedUserRegisterInputsModel(
                username = (results[0] as Success).value as Username,
                name = (results[1] as Success).value as Name,
                email = (results[2] as Success).value as Email,
                password = (results[3] as Success).value as Password
            )
        )
    } else {
        Failure(HTTPCodeException(errors.joinToString { it.message }, 400))
    }
}

fun validateUserLoginInput(input: UserLoginInputModel): Either<Exception, ValidatedUserLoginInputsModel> {
    val emailOrUsername = EmailOrUsername(input.emailOrUsername)
    val password = Password(input.password)

    if (password is Failure) {
        return Failure(password.value)
    }

    if (emailOrUsername is Failure) {
        return Failure(emailOrUsername.value)
    }

    return Success(
        ValidatedUserLoginInputsModel(
            emailOrUsername = (emailOrUsername as Success).value,
            password = (password as Success).value
        )
    )
}

fun validateUserEditInput(input: UserEditModel): Either<Exception, ValidatedUserEditInputsModel> {
    if(input.interests.size != input.interests.distinct().size) {
        return Failure(InterestsAreDuplicatedException())
    }

    val name = Name(input.name)
    if (name is Failure) return Failure(name.value)


    return Success(
        ValidatedUserEditInputsModel(
            name = (name as Success).value,
            description = input.description,
            interests = input.interests
        )
    )
}
