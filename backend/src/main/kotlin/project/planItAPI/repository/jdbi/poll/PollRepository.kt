package project.planItAPI.repository.jdbi.poll

import org.springframework.stereotype.Component
import project.planItAPI.models.PollOutputModel

@Component
interface PollRepository {

    fun createPoll(
        title: String,
        options: List<String>,
        duration: Int,
        eventId: Int,
        organizerId: Int
    ): Int?

    fun getPoll(pollId: Int): PollOutputModel?

    fun deletePoll(pollId: Int)

    fun getOption(optionId: Int): String?

    fun checkIfUserVoted(userId: Int, pollId: Int): Boolean

    fun vote(pollId: Int, userId: Int, optionId: Int)
}