package project.planItAPI.repository.user

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.JdbiException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.postgresql.ds.PGSimpleDataSource
import project.planItAPI.executeSQLScript
import project.planItAPI.repository.jdbi.users.JdbiUsersRepository
import project.planItAPI.repository.jdbi.utils.configureWithAppRequirements
import project.planItAPI.utils.RefreshTokenInfo


class JDBIRepoUserTests {

    companion object {
        private const val JDBI_URL = "jdbc:postgresql://localhost:5432/PlanItTestDatabase?user=postgres&password=123"
        private const val USER_SCRIPT_PATH = "src/test/sql/createUser.sql"
        private const val DELETE_USER_SCRIPT_PATH = "src/test/sql/clearUser.sql"

        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(JDBI_URL)
            }
        ).configureWithAppRequirements()

        @JvmStatic
        @AfterAll
        fun tearDown(){
            // Clean the database after each test.
            // This script will delete all the data from the users table.
            executeSQLScript(JDBI_URL, DELETE_USER_SCRIPT_PATH)
        }
    }

    /*
     * Functions for setting up and tearing down the database before and after each test.
     */
    @BeforeEach
    fun setup() {
        // Initialize database schema (create tables, etc.).
        // This script is a copy of the main SQL script that creates the tables in the database, but for the users only.
        executeSQLScript(JDBI_URL, USER_SCRIPT_PATH)
    }


    /*
     * Values for testing.
     */
    private val name = "John Doe"
    private val username = "johndoe"
    private val username2 = "johndoe2"
    private val email = "jd@mail.com"
    private val email2 = "jd2@mail.com"
    private val hashedPassword = "hashed_password"


    /**
     * Register Function Tests
     */
    @Nested
    inner class RegisterTests {
        @Test
        fun `simple registration`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                assertNotNull(user)
                assertEquals(user, 1)
            }

        @Test
        fun `several registrations`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                //Loop to create 10 registrations
                for (i in 1..10) {
                    val user = repo.register(name, username + "$i", email + "$i", hashedPassword)
                    assertNotNull(user)
                    assertEquals(user, i)
                }
            }

        @Test
        fun `should fail because of  repeated username`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)

                assertThrows<JdbiException> {
                    repo.register(name, username, email2, hashedPassword)
                }

            }

        @Test
        fun `should fail because of repeated email`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)

                assertThrows<JdbiException> {
                    repo.register(name, username2, email, hashedPassword)
                }

            }
    }

    /**
     * existsByUsername Function Tests
     */
    @Nested
    inner class ExistsByUsernameTests{
        @Test
        fun `existsByUsername should be false because there is no user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                assertFalse(repo.existsByUsername(username))

            }

        @Test
        fun `existsByUsername should be true because there is a user with the same username`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                assertTrue(repo.existsByUsername(username))

            }

        @Test
        fun `existsByUsername should be false because there is no user with the same username`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                assertFalse(repo.existsByUsername(username2))
            }
    }

    /**
     * getUserByEmail Function Tests
     */
    @Nested
    inner class GetUserByEmailTests{
        @Test
        fun `simple getUserByEmail test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val userByEmail = repo.getUserByEmail(email)
                if(userByEmail != null){
                    assertEquals(userByEmail.id, user)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserByEmail should return the current user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val user2ByEmail = repo.getUserByEmail(email2)
                if(user2ByEmail != null){
                    assertNotEquals(user2ByEmail.id, user1)
                    assertEquals(user2ByEmail.id, user2)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserByEmail should fail because there is no user in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val userByEmail = repo.getUserByEmail(email)
                assertEquals(userByEmail, null)
            }

        @Test
        fun `getUserByEmail should fail because there is no user with the same email`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                val userByEmail = repo.getUserByEmail(email2)
                assertEquals(userByEmail, null)
            }
    }

    /**
     * getUserByUsername Function Tests
     */
    @Nested
    inner class GetUserByUsername{
        @Test
        fun `simple userByUsername test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val userByUsername = repo.getUserByUsername(username)
                if(userByUsername != null){
                    assertEquals(userByUsername.id, user)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `userByUsername should return the current user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val user2ByUsername = repo.getUserByUsername(username2)
                if(user2ByUsername != null){
                    assertNotEquals(user2ByUsername.id, user1)
                    assertEquals(user2ByUsername.id, user2)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserByUsername should fail because there is no user in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val userByUsername = repo.getUserByUsername(username)
                assertEquals(userByUsername, null)
            }

        @Test
        fun `getUserByUsername should fail because there is no user with the same username`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                val userByUsername = repo.getUserByUsername(username2)
                assertEquals(userByUsername, null)
            }
    }

    /**
     * getUserIDByName Function Tests
     */
    @Nested
    inner class GetUserIDByNameTests {
        @Test
        fun `simple getUserIDByName test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val userByName = repo.getUserIDByName(username)
                if (userByName != null) {
                    assertEquals(userByName, user)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserIDByName should return the current user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val user2ByName = repo.getUserIDByName(username2)
                if (user2ByName != null) {
                    assertNotEquals(user2ByName, user1)
                    assertEquals(user2ByName, user2)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserIDByName should fail because there is no user in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val userByName = repo.getUserIDByName(username)
                assertEquals(userByName, null)
            }

        @Test
        fun `getUserIDByName should fail because there is no user with the same username`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                val userByName = repo.getUserIDByName(username2)
                assertEquals(userByName, null)
            }
    }

    /**
     * insertRefreshToken & getUserRefreshTokens Function Tests
     */
    @Nested
    inner class InsertRefreshToken{
        @Test
        fun `simple insertRefreshToken & getUserRefreshTokens test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                repo.insertRefreshToken(user!!, "token", java.sql.Timestamp(System.currentTimeMillis()))
                val tokens = repo.getUserRefreshTokens(user)
                if(tokens.isNotEmpty()){
                    assertEquals(tokens[0].token_validation, "token")
                } else {
                    assert(false)
                }
            }

        @Test
        fun `insertRefreshToken should fail because there is no user in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                assertThrows<JdbiException>{
                    repo.insertRefreshToken(1, "token", java.sql.Timestamp(System.currentTimeMillis()))
                }
            }

        @Test
        fun `getUserRefreshTokens should return an empty list`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val tokens = repo.getUserRefreshTokens(user!!)
                assertTrue(tokens.isEmpty())
            }

        @Test
        fun `getUserRefreshTokens should return a list with the current token`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                repo.insertRefreshToken(user!!, "token", java.sql.Timestamp(System.currentTimeMillis()))
                val tokens = repo.getUserRefreshTokens(user)
                if(tokens.isNotEmpty()){
                    assertEquals(tokens[0].token_validation, "token")
                    assertEquals(tokens.size, 1)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserRefreshTokens should return a list with the current token and another token`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val tokenString = "token"
                val tokenString2 = "token2"
                val tokenExpiration = java.sql.Timestamp(System.currentTimeMillis())
                repo.insertRefreshToken(user!!, tokenString, tokenExpiration)
                repo.insertRefreshToken(user, tokenString2, tokenExpiration)
                val tokens = repo.getUserRefreshTokens(user)
                if(tokens.isNotEmpty()){
                    assertEquals(tokens[0], RefreshTokenInfo(tokenString, tokenExpiration.toInstant()))
                    assertEquals(tokens[1], RefreshTokenInfo(tokenString2, tokenExpiration.toInstant()))
                    assertEquals(tokens.size, 2)
                } else {
                    assert(false)
                }
            }
    }

    /**
     * deleteUserRefreshToken Function Tests
     */
    @Nested
    inner class DeleteUserRefreshTests{
        @Test
        fun `simple deleteRefreshToken test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                repo.insertRefreshToken(user!!, "token", java.sql.Timestamp(System.currentTimeMillis()))
                val tokens = repo.getUserRefreshTokens(user)
                assertTrue(tokens.isNotEmpty())
                assertEquals(tokens.size, 1)
                repo.deleteUserRefreshToken(user, "token")
                val tokens2 = repo.getUserRefreshTokens(user)
                assertTrue(tokens2.isEmpty())
                assertEquals(tokens2.size, 0)
            }

        @Test
        fun `deleteRefreshToken should delete only the chosen token`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val token = "token"
                val token2 = "token2"
                val expirationDate = java.sql.Timestamp(System.currentTimeMillis())
                repo.insertRefreshToken(user!!, token, expirationDate)
                repo.insertRefreshToken(user, token2, expirationDate)
                repo.deleteUserRefreshToken(user, token)
                val tokens = repo.getUserRefreshTokens(user)
                if(tokens.isNotEmpty()){
                    assertEquals(tokens.size, 1)
                    assertEquals(tokens[0], RefreshTokenInfo(token2, expirationDate.toInstant()))
                } else {
                    assert(false)
                }
            }
    }

    /**
     * getUserIDByToken Function Tests
     */
    @Nested
    inner class GetUserIDByTokenTests{
        @Test
        fun `simple getUserIDByToken test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val token = "token"
                val expirationDate = java.sql.Timestamp(System.currentTimeMillis())
                repo.insertRefreshToken(user!!, token, expirationDate)
                val userID = repo.getUserIDByToken(token)
                if(userID != null){
                    assertEquals(userID, user)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUserIDByToken should return null because there is no user with the token`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val userID = repo.getUserIDByToken("token")
                assertEquals(userID, null)
            }

        @Test
        fun `getUserIDByToken should return null because there is no token in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                val userID = repo.getUserIDByToken("token")
                assertEquals(userID, null)
            }

        @Test
        fun `getUserIDByToken should return null because the token is not associated with the user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val token = "token"
                val expirationDate = java.sql.Timestamp(System.currentTimeMillis())
                repo.insertRefreshToken(user!!, token, expirationDate)
                val userID = repo.getUserIDByToken("token2")
                assertEquals(userID, null)
            }

        @Test
        fun `getUserIDByToken should return the correct user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val token = "token"
                val token2 = "token2"
                val expirationDate = java.sql.Timestamp(System.currentTimeMillis())
                repo.insertRefreshToken(user1!!, token, expirationDate)
                repo.insertRefreshToken(user2!!, token2, expirationDate)
                val userID1 = repo.getUserIDByToken(token)
                val userID2 = repo.getUserIDByToken(token2)
                if(userID1 != null){
                    assertEquals(userID1, user1)
                    assertNotEquals(userID1, user2)
                    assertEquals(userID2, user2)
                    assertNotEquals(userID2, user1)
                } else {
                    assert(false)
                }
            }
    }

    /**
     * getUser Function Tests
     */
    @Nested
    inner class GetUserTests{
        @Test
        fun `simple getUser test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val userRepo = repo.getUser(user!!)
                if(userRepo != null){
                    assertEquals(userRepo.id, user)
                    assertEquals(userRepo.name, name)
                    assertEquals(userRepo.username, username)
                    assertEquals(userRepo.email, email)
                    assertEquals(userRepo.description, "")
                    assertEquals(userRepo.interests, "")
                } else {
                    assert(false)
                }
            }

        @Test
        fun `getUser should return null because there is no user in the database`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                assertEquals( repo.getUser(1), null )
            }

        @Test
        fun `getUser should return null because there is no user with the same id`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                repo.register(name, username, email, hashedPassword)
                assertEquals( repo.getUser(2), null )
            }

        @Test
        fun `getUser should return the correct user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val userRepo1 = repo.getUser(user1!!)
                val userRepo2 = repo.getUser(user2!!)
                if(userRepo1 != null && userRepo2 != null){
                    assertEquals(userRepo1.id, user1)
                    assertEquals(userRepo1.name, name)
                    assertEquals(userRepo1.username, username)
                    assertEquals(userRepo1.email, email)
                    assertEquals(userRepo1.description, "")
                    assertEquals(userRepo1.interests, "")
                    assertEquals(userRepo2.id, user2)
                    assertEquals(userRepo2.name, name)
                    assertEquals(userRepo2.username, username2)
                    assertEquals(userRepo2.email, email2)
                    assertEquals(userRepo2.description, "")
                    assertEquals(userRepo2.interests, "")
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
        fun `simple editUser test`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user = repo.register(name, username, email, hashedPassword)
                val newName = "Jane Doe"
                val newDescription = "Description"
                val newInterests = "Interests"
                repo.editUser(user!!, newName, newDescription, newInterests)
                val userRepo = repo.getUser(user)
                if (userRepo != null) {
                    assertEquals(userRepo.id, user)
                    assertEquals(userRepo.name, newName)
                    assertEquals(userRepo.username, username)
                    assertEquals(userRepo.email, email)
                    assertEquals(userRepo.description, newDescription)
                    assertEquals(userRepo.interests, newInterests)
                } else {
                    assert(false)
                }
            }

        @Test
        fun `editUser should return the correct user`() =
            runWithHandle { handle ->
                val repo = JdbiUsersRepository(handle)
                val user1 = repo.register(name, username, email, hashedPassword)
                val user2 = repo.register(name, username2, email2, hashedPassword)
                val newName1 = "Jane Doe"
                val newDescription1 = "Description"
                val newInterests1 = "Interests"
                val newName2 = "Jane Doe2"
                val newDescription2 = "Description2"
                val newInterests2 = "Interests2"
                repo.editUser(user1!!, newName1, newDescription1, newInterests1)
                repo.editUser(user2!!, newName2, newDescription2, newInterests2)
                val userRepo1 = repo.getUser(user1)
                val userRepo2 = repo.getUser(user2)
                if (userRepo1 != null && userRepo2 != null) {
                    assertEquals(userRepo1.id, user1)
                    assertEquals(userRepo1.name, newName1)
                    assertEquals(userRepo1.username, username)
                    assertEquals(userRepo1.email, email)
                    assertEquals(userRepo1.description, newDescription1)
                    assertEquals(userRepo1.interests, newInterests1)
                    assertEquals(userRepo2.id, user2)
                    assertEquals(userRepo2.name, newName2)
                    assertEquals(userRepo2.username, username2)
                    assertEquals(userRepo2.email, email2)
                    assertEquals(userRepo2.description, newDescription2)
                    assertEquals(userRepo2.interests, newInterests2)
                } else {
                    assert(false)
                }
            }
    }
}


