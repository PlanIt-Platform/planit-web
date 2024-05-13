package project.planItAPI.repository.transaction

import org.jdbi.v3.core.Handle
import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.event.JdbiEventsRepository
import project.planItAPI.repository.jdbi.poll.JdbiPollRepository
import project.planItAPI.repository.jdbi.poll.PollRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.repository.jdbi.user.JdbiUsersRepository

class JdbiTransaction(
    private val handle: Handle
) : Transaction {

    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val eventsRepository: EventsRepository = JdbiEventsRepository(handle)
    override val pollRepository: PollRepository = JdbiPollRepository(handle)
}