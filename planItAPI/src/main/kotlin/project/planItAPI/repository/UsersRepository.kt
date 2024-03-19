package project.planItAPI.repository

import org.springframework.stereotype.Component

@Component
interface UsersRepository {

    /**
     * Registers a new user with the provided information.
     *
     * @param name The username of the new user.
     * @param email The email address of the new user.
     * @param password The hashed password of the new user.
     * @return The ID of the newly registered user, or null if registration fails.
     */
    fun register(name: String, email: String, password: String): Int?
}