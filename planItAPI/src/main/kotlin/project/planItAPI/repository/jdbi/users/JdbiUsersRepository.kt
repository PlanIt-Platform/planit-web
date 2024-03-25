package project.planItAPI.repository.jdbi.users

import org.jdbi.v3.core.Handle
import project.planItAPI.repository.UsersRepository
import project.planItAPI.utils.RefreshTokenInfo
import project.planItAPI.utils.UserLogInValidation
import java.sql.Timestamp

class JdbiUsersRepository(private val handle: Handle) : UsersRepository {

    override fun register(name: String, email: String, password: String): Int? =
        handle.createUpdate(
            "insert into dbo.users(username, hashed_password, email) " +
                    "values (:username, :password, :email)",
        )
            .bind("username", name)
            .bind("password", password)
            .bind("email", email)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()


    override fun existsByUsername(name: String): Boolean =
        handle.createQuery(
            "select id from dbo.Users where username = :name",
        )
            .bind("name", name)
            .mapTo(Int::class.java).singleOrNull() != null


    override fun getUserByEmail(email: String): UserLogInValidation? =
        handle.createQuery(
            "select id, username, password_validation from dbo.Users where email = :email",
        )
            .bind("email", email)
            .mapTo(UserLogInValidation::class.java)
            .singleOrNull()

    override fun getUserByUsername(name: String): UserLogInValidation? =
        handle.createQuery(
            "select id, username, password_validation from dbo.Users where username = :name",
        )
            .bind("name", name)
            .mapTo(UserLogInValidation::class.java)
            .singleOrNull()


    override fun getUserIDByName(name: String): Int? =
        handle.createQuery(
            "select id from dbo.Users where username = :name",
        )
            .bind("name", name)
            .mapTo(Int::class.java)
            .singleOrNull()


    override fun insertRefreshToken(id: Int, token: String, expirationDate: Timestamp) {
        handle.createUpdate(
            "insert into dbo.refreshtokens(token_validation, user_id, expiration_date) " +
                    "values (:token, :id, :date)",
        )
            .bind("token", token)
            .bind("id", id)
            .bind("date", expirationDate)
            .execute()
    }


    override fun getUserRefreshTokens(userID: Int): List<RefreshTokenInfo> =
        handle.createQuery(
            "select token_validation, expiration_date from dbo.RefreshTokens " +
                    "where user_id = :id " +
                    "order by expiration_date DESC",
        )
            .bind("id", userID)
            .mapTo(RefreshTokenInfo::class.java)
            .list()


    override fun deleteUserRefreshToken(userID: Int, hashedToken: String) {
        handle.createUpdate(
            "DELETE FROM dbo.RefreshTokens " +
                    "WHERE user_id = :id AND token_validation = :token",
        )
            .bind("token", hashedToken)
            .bind("id", userID)
            .execute()

    }


    override fun getUserIDByToken(refreshToken: String): Int? =
        handle.createQuery("select user_id from dbo.refreshtokens where token_validation = :token")
            .bind("token", refreshToken)
            .mapTo(Int::class.java)
            .singleOrNull()
}