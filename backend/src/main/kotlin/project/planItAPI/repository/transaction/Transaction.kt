package project.planItAPI.repository.transaction

import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.poll.PollRepository
import project.planItAPI.repository.jdbi.user.UsersRepository

/**
 * Interface representing a transaction that involves interactions with multiple repositories.
 */
interface Transaction {

    /**
     * The repository for managing user-related data.
     */
    val usersRepository: UsersRepository

    /**
     * The repository for managing event-related data.
     */
    val eventsRepository: EventsRepository

    /**
     * The repository for managing poll-related data.
     */
    val pollRepository: PollRepository
}
