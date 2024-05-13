package project.planItAPI.services.poll

import org.springframework.stereotype.Service
import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat
import project.planItAPI.models.SuccessMessage
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreatePollException
import project.planItAPI.utils.InvalidNumberOfOptionsException
import project.planItAPI.utils.OptionNotFoundException
import project.planItAPI.utils.PollNotFoundException
import project.planItAPI.utils.UserAlreadyVotedException
import project.planItAPI.utils.UserIsNotOrganizerException

@Service
class PollServices(
    private val transactionManager: TransactionManager
) {

    fun createPoll(
        title: String,
        options: List<Option>,
        duration: TimeFormat,
        organizerId: Int,
        eventId: Int
    ): CreatePollResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            if (options.size in 2..5) throw InvalidNumberOfOptionsException()
            if (eventsRepository.getEventOrganizer(eventId) != organizerId) throw UserIsNotOrganizerException()
            return@run pollRepository.createPoll(
                title,
                options.map { opt -> opt.value },
                duration.value.toInt(),
                eventId,
                organizerId
            ) ?: throw FailedToCreatePollException()
        }

    fun getPoll(pollId: Int, eventId: Int): GetPollResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            return@run pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
        }

    fun deletePoll(pollId: Int, eventId: Int, userId: Int): DeletePollResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (eventsRepository.getEventOrganizer(event.id) != userId) throw UserIsNotOrganizerException()
            pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
            pollRepository.deletePoll(pollId)
            return@run SuccessMessage("Poll deleted successfully")
        }

    fun votePoll(userId: Int, pollId: Int, optionId: Int): VoteResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
            pollRepository.getOption(optionId) ?: throw OptionNotFoundException()
            val userVoteExists = it.pollRepository.checkIfUserVoted(userId, pollId)
            if (userVoteExists) {
                throw UserAlreadyVotedException()
            }
            pollRepository.vote(pollId, userId, optionId)
            return@run SuccessMessage("Vote registered successfully")
        }
}