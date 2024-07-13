package project.planItAPI.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrUsername
import project.planItAPI.domain.user.Name
import project.planItAPI.domain.user.Password
import project.planItAPI.domain.user.Username
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.utils.FakeTransactionManager
import project.planItAPI.services.utils.FakeUserServices
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserNotFoundException

@SpringBootTest
class UserServicesTests {

    @MockBean
    private lateinit var usersRepository: UsersRepository

    private lateinit var userServices: FakeUserServices

    @BeforeEach
    fun setUp() {
        val fakeTransactionManager = FakeTransactionManager(usersRepository)
        userServices = FakeUserServices(fakeTransactionManager)
    }

    private val name = (Name("testUser") as Success).value
    private val username = (Username("testUser") as Success).value
    private val email = (Email("testUser@mail.com") as Success).value
    private val password = (Password("t3stP@ssw0rd") as Success).value

    /**
     * Register Function Tests
     */
    @Nested
    inner class RegisterTests {
        @Test
        fun `can register a new user`() {
            val result = userServices.register(name, username, email, password)
            if (result is Success) {
                assertEquals(name.value, result.value.name)
                assertEquals(username.value, result.value.username)
                assertEquals(1, result.value.id)
                println("User registered successfully")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register a user with an existing email`() {
            userServices.register(name, username, email, password)
            val result = userServices.register(name, (Username("testUser2") as Success).value, email, password)
            if (result is Failure) {
                assert(result.value is ExistingEmailException)
                println("User with existing email cannot be registered")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register a user with an existing username`() {
            userServices.register(name, username, email, password)
            val result = userServices.register(name, username, (Email("testUser2@mail.com") as Success).value, password)
            if (result is Failure) {
                assert(result.value is ExistingUsernameException)
                println("User with existing username cannot be registered")
            } else {
                assert(false)
            }
        }
    }

    /**
     * Login Function Tests
     */
    @Nested
    inner class LoginTests {
        @Test
        fun `can login a user with email`() {
            userServices.register(name, username, email, password)
            val result = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            assert(result is Success)
        }

        @Test
        fun `can login a user with username`() {
            userServices.register(name, username, email, password)
            val result = userServices.login((EmailOrUsername(username.value) as Success).value, password)
            assert(result is Success)
        }

        @Test
        fun `cannot login a user because there is no user`() {
            val result = userServices.login((EmailOrUsername(username.value) as Success).value, password)
            assert(result is Failure)
        }

        @Test
        fun `cannot login a user because there is no user with the email`() {
            userServices.register(name, username, email, password)
            val result = userServices.login((EmailOrUsername("testUsr@mail.com") as Success).value, password)
            assert(result is Failure)
        }

        @Test
        fun `cannot login a user because there is no user with the username`() {
            userServices.register(name, username, email, password)
            val result = userServices.login((EmailOrUsername("testUsr") as Success).value, password)
            assert(result is Failure)
        }
    }

    /**
     * Logout Function Tests
     */
    @Nested
    inner class LogoutTests {
        @Test
        fun `can logout a user`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.logout(loginResult.value.accessToken, loginResult.value.refreshToken)
                assert(result is Success)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot logout a user because of wrong refresh token`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.logout(loginResult.value.accessToken, "wrongRefreshToken")
                assert(result is Failure)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot logout a user because of wrong access and refresh tokens`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.logout("wrongAccessToken", "wrongRefreshToken")
                assert(result is Failure)
            } else {
                assert(false)
            }
        }
    }

    /**
     * getUser Function Tests
     */
    @Nested
    inner class GetUserTests {
        @Test
        fun `getUser successful`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.getUser(1)
                if (result is Success) {
                    assertEquals(name.value, result.value.name)
                    assertEquals(username.value, result.value.username)
                    assertEquals(email.value, result.value.email)
                    assertEquals(1, result.value.id)
                    assertEquals("", result.value.description)
                    assertEquals(emptyList<String>(), result.value.interests)
                } else {
                    assert(false)
                }
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot getUser because no user exists`() {
            val result = userServices.getUser(1)
            assert(result is Failure)
            if (result is Failure) {
                assert(result.value is UserNotFoundException)
            }
        }

        @Test
        fun `cannot getUser because of wrong id`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.getUser(2)
                assert(result is Failure)
                if (result is Failure) {
                    assert(result.value is UserNotFoundException)
                }
            } else {
                assert(false)
            }
        }
    }

    /**
     * editUser Function Tests
     */
    @Nested
    inner class EditUserTests {
        @Test
        fun `editUser successful`() {
            userServices.register(name, username, email, password)
            val newName = (Name("newName") as Success).value
            val newDescription = "newDescription"
            val newInterests = listOf("Football", "Games")
            userServices.editUser(1, newName, newDescription, newInterests)
            val editedUser = userServices.getUser(1)
            if (editedUser is Success) {
                assertEquals(newName.value, editedUser.value.name)
                assertEquals(username.value, editedUser.value.username)
                assertEquals(email.value, editedUser.value.email)
                assertEquals(1, editedUser.value.id)
                assertEquals(newDescription, editedUser.value.description)
                assertEquals(newInterests, editedUser.value.interests)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot editUser because no user exists`() {
            val result = userServices.editUser(1, name, "", listOf("Football", "Games"))
            assert(result is Failure)
            if (result is Failure) {
                assert(result.value is UserNotFoundException)
            }
        }

        @Test
        fun `cannot editUser because of wrong id`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login((EmailOrUsername(email.value) as Success).value, password)
            if (loginResult is Success) {
                val result = userServices.editUser(2, name, "", listOf("Football", "Games"))
                assert(result is Failure)
                if (result is Failure) {
                    assert(result.value is UserNotFoundException)
                }
            } else {
                assert(false)
            }
        }
    }
}