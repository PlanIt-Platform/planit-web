package project.planItAPI.repository.jdbi.users

import org.jdbi.v3.core.Handle
import project.planItAPI.utils.RefreshTokenInfo
import project.planItAPI.utils.UserInfo
import project.planItAPI.utils.UserInfoRepo
import project.planItAPI.utils.UserLogInValidation
import java.sql.Timestamp

class JdbiUsersRepository(private val handle: Handle) : UsersRepository {

    override fun register(name: String, username: String, email: String, password: String): Int? =
        handle.createUpdate(
            "insert into dbo.users(name, username, hashed_password, email) " +
                    "values (:name, :username, :password, :email)",
        )
            .bind("name", name)
            .bind("username", username)
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
            "select id, username, hashed_password from dbo.Users where email = :email",
        )
            .bind("email", email)
            .mapTo(UserLogInValidation::class.java)
            .singleOrNull()

    override fun getUserByUsername(username: String): UserLogInValidation? =
        handle.createQuery(
            "select id, username, hashed_password from dbo.Users where username = :name",
        )
            .bind("name", username)
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


    override fun getUser(id: Int): UserInfoRepo? {
        return handle.createQuery(
            "select id, name, username, description, profile_picture, profile_picture_type from dbo.Users where id = :id",
        )
            .bind("id", id)
            .mapTo(UserInfoRepo::class.java)
            .singleOrNull()
    }


    override fun uploadProfilePicture(id: Int, picture: ByteArray, fileType: String): Int? =
        handle.createUpdate(
            "update dbo.Users set profile_picture = :picture and profile_picture_type = :fileType where id = :id",
        )
            .bind("picture", picture)
            .bind("fileType", fileType)
            .bind("id", id)
            .execute()

}