package project.planItAPI.repository.transaction

import project.planItAPI.repository.jdbi.events.EventsRepository
import project.planItAPI.repository.jdbi.users.UsersRepository

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
}
