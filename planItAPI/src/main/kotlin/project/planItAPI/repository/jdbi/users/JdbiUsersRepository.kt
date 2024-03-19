package project.planItAPI.repository.jdbi.users

import org.jdbi.v3.core.Handle
import project.planItAPI.repository.UsersRepository

class JdbiUsersRepository(private val handle: Handle) : UsersRepository {
}