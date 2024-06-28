package project.planItAPI.services.poll

import org.springframework.stereotype.Service
import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat
import project.planItAPI.models.CreatePollOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.services.dateToMilliseconds
import project.planItAPI.services.getNowTime
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreatePollException
import project.planItAPI.utils.OptionNotFoundException
import project.planItAPI.utils.PollHasEndedException
import project.planItAPI.utils.PollNotFoundException
import project.planItAPI.utils.UserAlreadyVotedException
import project.planItAPI.utils.UserIsNotOrganizerException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
            if (organizerId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
            val pollId = pollRepository.createPoll(
                title,
                options.map { opt -> opt.value },
                duration.value.toInt(),
                eventId,
                organizerId
            ) ?: throw FailedToCreatePollException()

            return@run CreatePollOutputModel(pollId, title)
        }

    fun getPoll(pollId: Int, eventId: Int): GetPollResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            return@run pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
        }

    fun deletePoll(userId: Int, pollId: Int, eventId: Int): DeletePollResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (userId !in eventsRepository.getEventOrganizers(event.id)) throw UserIsNotOrganizerException()
            pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
            pollRepository.deletePoll(pollId)
            return@run SuccessMessage("Poll deleted successfully")
        }

    fun votePoll(userId: Int, pollId: Int, optionId: Int, eventId: Int): VoteResult =
        transactionManager.run {
            val pollRepository = it.pollRepository
            val eventsRepository = it.eventsRepository
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
            val poll = pollRepository.getPoll(pollId) ?: throw PollNotFoundException()
            val nowTime = System.currentTimeMillis()
            val endTime = dateToMilliseconds(poll.created_at) + poll.duration * 60 * 60 * 1000
            if (nowTime > endTime) {
                throw PollHasEndedException()
            }
            pollRepository.getOption(optionId) ?: throw OptionNotFoundException()
            val userVoteExists = it.pollRepository.checkIfUserVoted(userId, pollId)
            if (userVoteExists) {
                throw UserAlreadyVotedException()
            }
            pollRepository.vote(pollId, userId, optionId)
            return@run SuccessMessage("Vote registered successfully")
        }

    fun getPolls(eventId: Int): GetPollsResult =
        transactionManager.run {
            val eventsRepository = it.eventsRepository
            eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            return@run it.pollRepository.getPolls(eventId)
        }
}