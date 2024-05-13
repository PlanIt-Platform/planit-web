package project.planItAPI.http.controllers.poll

import project.planItAPI.domain.Id
import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat
import project.planItAPI.models.PollInputModel
import project.planItAPI.models.ValidatedPollInputModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success


fun validatePollInput(input: PollInputModel): Either<Exception, ValidatedPollInputModel> {
    for (option in input.options) {
        val result = Option(option)
        if (result is Failure) return Failure(result.value)
    }

    val duration = TimeFormat(input.duration)
    if (duration is Failure) return Failure(duration.value)

    return Success(
        ValidatedPollInputModel(
            title = input.title,
            options = input.options.map { Option(it) as Success }.map { it.value },
            duration = (duration as Success).value
        )
    )
}