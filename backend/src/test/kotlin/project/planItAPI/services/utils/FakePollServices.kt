package project.planItAPI.services.utils

import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat
import project.planItAPI.models.CreatePollOutputModel
import project.planItAPI.models.OptionVotesModel
import project.planItAPI.models.PollOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.services.poll.CreatePollResult
import project.planItAPI.services.poll.DeletePollResult
import project.planItAPI.services.poll.GetPollResult
import project.planItAPI.services.poll.GetPollsResult
import project.planItAPI.services.poll.PollServices
import project.planItAPI.services.poll.VoteResult
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.OptionNotFoundException
import project.planItAPI.utils.PollNotFoundException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserAlreadyVotedException
import project.planItAPI.utils.UserIsNotOrganizerException

class FakePollServices(transactionManager: TransactionManager) : PollServices(transactionManager) {

    private fun generatePollID(): Int {
        // Simulate generating a poll ID
        return 1 // Just a placeholder value for demonstration purposes
    }

    override fun createPoll(
        title: String,
        options: List<Option>,
        duration: TimeFormat,
        organizerId: Int,
        eventId: Int
    ): CreatePollResult {
        // Simulate the behavior of creating a poll
        if (eventId == -1) {
            return Failure(EventNotFoundException())
        }

        if (organizerId != 1) {
            return Failure(UserIsNotOrganizerException())
        }
        val pollID = generatePollID()
        return Success(CreatePollOutputModel(pollID, title))
    }

    override fun getPoll(pollId: Int, eventId: Int): GetPollResult {
        // Simulate the behavior of retrieving a poll
        if (eventId == -1) {
            return Failure(EventNotFoundException())
        }
        return if (pollId == 1) {
            val createdAt = "2021-10-01T12:00:00Z"
            Success(
                PollOutputModel(
                pollId,
                    "Sample Poll",
                    createdAt,
                    4,
                    listOf(
                    OptionVotesModel(1, "Option 1", 2),
                    OptionVotesModel(2, "Option 2", 1),
                    )
            ))
        } else {
             Failure(PollNotFoundException())
        }
    }

    override fun deletePoll(userId: Int, pollId: Int, eventId: Int): DeletePollResult {
        // Simulate the behavior of deleting a poll
        if (eventId == -1) {
            return Failure(EventNotFoundException())
        }
        if (userId != 1) {
            return Failure(UserIsNotOrganizerException())
        }
        if (pollId != 1) {
            return Failure(PollNotFoundException())
        }
        return Success(SuccessMessage("Poll deleted successfully"))
    }

    override fun votePoll(userId: Int, pollId: Int, optionId: Int, eventId: Int): VoteResult {
        // Simulate the behavior of voting in a poll
        if (eventId == -1) {
            return Failure(EventNotFoundException())
        }
        if (pollId != 1) {
            return Failure(PollNotFoundException())
        }
        if (optionId != 1) {
            return Failure(OptionNotFoundException())
        }
        if (userId == 1) {
            return Failure(UserAlreadyVotedException())
        }
        return Success(SuccessMessage("Vote registered successfully"))
    }

    override fun getPolls(eventId: Int): GetPollsResult {
        // Simulate the behavior of retrieving polls for an event
        if (eventId == -1) {
            return Failure(EventNotFoundException())
        }
        return Success(
            listOf(
                PollOutputModel(
                    1,
                    "Sample Poll",
                    "2021-10-01T12:00:00Z",
                    4,
                    listOf(
                        OptionVotesModel(1, "Option 1", 2),
                        OptionVotesModel(2, "Option 2", 1),
                    )
                ),
                PollOutputModel(
                    2,
                    "Another Poll",
                    "2021-10-02T12:00:00Z",
                    3,
                    listOf(
                        OptionVotesModel(3, "Option 1", 1),
                        OptionVotesModel(4, "Option 2", 2),
                    )
                )
            )
        )
    }
}
