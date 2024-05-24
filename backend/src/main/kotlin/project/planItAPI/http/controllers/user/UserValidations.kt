package project.planItAPI.http.controllers.user

import project.planItAPI.domain.event.Category
import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrName
import project.planItAPI.domain.user.Password
import project.planItAPI.domain.user.Name
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
        Name(input.username),
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
                username = (results[0] as Success).value as Name,
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
    val emailOrName = EmailOrName(input.emailOrName)
    val password = Password(input.password)

    if (password is Failure) {
        return Failure(password.value)
    }

    if (emailOrName is Failure) {
        return Failure(emailOrName.value)
    }

    return Success(
        ValidatedUserLoginInputsModel(
            emailOrName = (emailOrName as Success).value,
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

    for (category in input.interests) {
        val result = Category(category)
        if (result is Failure) return Failure(result.value)
    }

    return Success(
        ValidatedUserEditInputsModel(
            name = (name as Success).value,
            description = input.description,
            interests = input.interests.map { Category(it) as Success }.map { it.value }
        )
    )
}