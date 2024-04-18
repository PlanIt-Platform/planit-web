package project.planItAPI.services

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import project.planItAPI.executeSQLScript
import project.planItAPI.repository.jdbi.utils.configureWithAppRequirements
import project.planItAPI.repository.jdbi.utils.users.UsersDomain
import project.planItAPI.repository.jdbi.utils.users.UsersDomainConfig
import project.planItAPI.repository.transaction.JdbiTransactionManager
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.MultiplePasswordExceptions
import project.planItAPI.utils.ServerConfiguration
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserNotFoundException
import java.time.Duration

class UserServicesTests {

    companion object {
        private const val JDBI_URL = "jdbc:postgresql://localhost:5432/PlanItTestDatabase?user=postgres&password=123"
        private const val USER_SCRIPT_PATH = "src/test/sql/createUser.sql"
        private const val DELETE_USER_SCRIPT_PATH = "src/test/sql/clearUser.sql"

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(JDBI_URL)
            }
        ).configureWithAppRequirements()

        private val transactionManager = JdbiTransactionManager(jdbi)

        private val usersDomainConfig = UsersDomainConfig(
            7,
            Duration.ofHours(24),
            Duration.ofHours(168),
            1)

        private val serverConfig = ServerConfiguration(
            "mRk4Tl7Y5vtcmRn60C1mbLEGdNbaqPKtFPl0NY85geFZwu4uQfIOKajHu",
            "lbtiqTjif4y18LXlMEgDsEwkQiuIcD041lu7hmKxDvmYtgVM6JS",
            "J8Bon8MoizjQijRTHMl1JpcIYYbaNRU279Vvef9onhhmWhFb1CZIQ7szXT9xnYcWZ",
            "9okxeepSaFoj2nkn4doAb9yW5iKbvfz7Ro84w5HIbwOMBNcMP6PMQEeh9"
        )

        private val usersDomain = UsersDomain(usersDomainConfig, serverConfig)

        private val userServices = UsersServices(transactionManager, usersDomain, usersDomainConfig)

        @JvmStatic
        @AfterAll
        fun tearDown(){
            // Clean the database after each test.
            // This script will delete all the data from the users table.
            executeSQLScript(JDBI_URL, DELETE_USER_SCRIPT_PATH)
        }
    }

    @BeforeEach
    fun setUp(){
        // Clean the database before each test.
        // This script will delete all the data from the users table.
        executeSQLScript(JDBI_URL, USER_SCRIPT_PATH)
    }

    private val name = "testUser"
    private val username = "testUser"
    private val email = "testUser@mail.com"
    private val password = "t3stP@ssw0rd"


    /**
     * Register Function Tests
     */
    @Nested
    inner class RegisterTests{
        @Test
        fun `can register a new user`() {
            val result = userServices.register(name, username, email, password)
            if (result is Success) {
                assertEquals(name, result.value.name)
                assertEquals(username, result.value.username)
                assertEquals(1, result.value.id)
                println("User registered successfully")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register a user with an existing email`() {
            userServices.register(name, username, email, password)
            val result = userServices.register(name, "testUser2", email, password)
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
            val result = userServices.register(name, username, "testUser2@mail.com", password)
            if (result is Failure) {
                assert(result.value is ExistingUsernameException)
                println("User with existing username cannot be registered")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register user because of short password`() {
            val result = userServices.register(name, username, email, "P1@")
            if (result is Failure) {
                assert(result.value is MultiplePasswordExceptions)
                println("User could not be registered")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register user because of no numbers in password`() {
            val result = userServices.register(name, username, email, "P@ssword")
            if (result is Failure) {
                assert(result.value is MultiplePasswordExceptions)
                println("User could not be registered")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register user because of no special chars in password`() {
            val result = userServices.register(name, username, email, "Passw0rd")
            if (result is Failure) {
                assert(result.value is MultiplePasswordExceptions)
                println("User could not be registered")
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot register user because of no uppercase chars in password`() {
            val result = userServices.register(name, username, email, "p@ssword1")
            if (result is Failure) {
                assert(result.value is MultiplePasswordExceptions)
                println("User could not be registered")
            } else {
                assert(false)
            }
        }
    }

    /**
     * Login Function Tests
     */
    @Nested
    inner class LoginTests{
        @Test
        fun `can login a user with email`() {
            userServices.register(name, username, email, password)
            val result = userServices.login(email, password)
            assert(result is Success)
        }

        @Test
        fun `can login a user with username`() {
            userServices.register(name, username, email, password)
            val result = userServices.login(username, password)
            assert(result is Success)
        }

        @Test
        fun `cannot login a user with wrong password`() {
            userServices.register(name, username, email, password)
            val result = userServices.login(email, "wrongPassword")
            assert(result is Failure)
        }

        @Test
        fun `cannot login a user because there is no user`() {
            val result = userServices.login(email, password)
            assert(result is Failure)
        }

        @Test
        fun `cannot login a user because there is no user with the email`() {
            userServices.register(name, username, email, password)
            val result = userServices.login("testUsr@mail.com", password)
            assert(result is Failure)
        }

        @Test
        fun `cannot login a user because there is no user with the username`() {
            userServices.register(name, username, email, password)
            val result = userServices.login("testUsr", password)
            assert(result is Failure)
        }
    }

    /**
     * Logout Function Tests
     */
    @Nested
    inner class LogoutTests{
        @Test
        fun `can logout a user`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login(email, password)
            if (loginResult is Success) {
                val result = userServices.logout(loginResult.value.accessToken, loginResult.value.refreshToken)
                assert(result is Success)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot logout a user because of wrong access token`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login(email, password)
            if (loginResult is Success) {
                val result = userServices.logout("wrongAccessToken", loginResult.value.refreshToken)
                assert(result is Failure)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot logout a user because of wrong refresh token`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login(email, password)
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
            val loginResult = userServices.login(email, password)
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
            val loginResult = userServices.login(email, password)
            if (loginResult is Success) {
                val result = userServices.getUser(1)
                if (result is Success) {
                    assertEquals(name, result.value.name)
                    assertEquals(username, result.value.username)
                    assertEquals(email, result.value.email)
                    assertEquals(1, result.value.id)
                    assertEquals("", result.value.description)
                    assertEquals(listOf(""), result.value.interests)
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
            val loginResult = userServices.login(email, password)
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
    inner class EditUserTests{
        @Test
        fun `editUser successful`() {
            userServices.register(name, username, email, password)
            val newName = "newName"
            val newDescription = "newDescription"
            val newInterests = "newInterests"
            userServices.editUser(1, newName, newDescription, newInterests)
            val editedUser = userServices.getUser(1)
            if (editedUser is Success) {
                assertEquals(newName, editedUser.value.name)
                assertEquals(username, editedUser.value.username)
                assertEquals(email, editedUser.value.email)
                assertEquals(1, editedUser.value.id)
                assertEquals(newDescription, editedUser.value.description)
                assertEquals(listOf(newInterests), editedUser.value.interests)
            } else {
                assert(false)
            }
        }

        @Test
        fun `cannot editUser because no user exists`() {
            val result = userServices.editUser(1, name, "", "")
            assert(result is Failure)
            if (result is Failure) {
                assert(result.value is UserNotFoundException)
            }
        }

        @Test
        fun `cannot editUser because of wrong id`() {
            userServices.register(name, username, email, password)
            val loginResult = userServices.login(email, password)
            if (loginResult is Success) {
                val result = userServices.editUser(2, name, "", "")
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
     * about Function Tests
     */
    @Nested
    inner class AboutTests{
        @Test
        fun `about successful`() {
            val result = userServices.about()
            assertEquals(result.name, userServices.APPLICATION_NAME)
            assertEquals(result.version, userServices.APP_VERSION)
            assertEquals(result.contributors, userServices.CONTRIBUTORS_LIST)
        }
    }
}