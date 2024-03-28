package project.planItAPI.repository.transaction

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
     * Rolls back the changes made during the transaction if exceptions occur
     */
    fun rollback()
}
