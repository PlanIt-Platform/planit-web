package project.planItAPI.models

import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat

data class PollInputModel(
    val title: String,
    val options: List<String>,
    val duration: String
)

data class ValidatedPollInputModel(
    val title: String,
    val options: List<Option>,
    val duration: TimeFormat
)

data class PollOutputModel(
    val id: Int,
    val title: String,
    val created_at: String,
    val duration: Int,
    val options: List<OptionVotesModel>
)

data class CreatePollOutputModel(
    val id: Int,
    val title: String
)

data class OptionVotesModel(
    val id: Int,
    val title: String,
    val votes: Int
)