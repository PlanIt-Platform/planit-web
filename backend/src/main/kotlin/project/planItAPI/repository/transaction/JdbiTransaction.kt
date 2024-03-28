package project.planItAPI.repository.transaction

import org.jdbi.v3.core.Handle
import project.planItAPI.repository.jdbi.users.UsersRepository
import project.planItAPI.repository.jdbi.users.JdbiUsersRepository

class JdbiTransaction(
    private val handle: Handle
) : Transaction {

    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}