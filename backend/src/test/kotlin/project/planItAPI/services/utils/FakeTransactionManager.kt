package project.planItAPI.services.utils

import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.poll.PollRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.repository.transaction.Transaction
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.Either
import project.planItAPI.utils.Success

class FakeTransaction(
    override val usersRepository: UsersRepository,
    override val eventsRepository: EventsRepository,
    override val pollRepository: PollRepository
) : Transaction

class FakeTransactionManager(
    private val usersRepository: UsersRepository,
    private val eventsRepository: EventsRepository? = null,
    private val pollRepository: PollRepository? = null
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): Either<Exception, R> {
        // Create a FakeTransaction instance
        val transaction = FakeTransaction(usersRepository, eventsRepository!!, pollRepository!!)
        return Success(block(transaction))
    }
}