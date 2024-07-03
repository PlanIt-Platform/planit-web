package project.planItAPI.repository.jdbi.user

import org.jdbi.v3.core.Handle
import project.planItAPI.models.RefreshTokenInfo
import project.planItAPI.models.RoleOutputModel
import project.planItAPI.models.SearchEventsOutputModel
import project.planItAPI.models.UserInfoRepo
import project.planItAPI.models.UserLogInValidation
import java.sql.Timestamp

class JdbiUsersRepository(private val handle: Handle) : UsersRepository {

    override fun register(name: String, username: String, email: String, hashed_password: String): Int? =
        handle.createUpdate(
            "insert into dbo.users(name, username, hashed_password, email) " +
                    "values (:name, :username, :hashed_password, :email)",
        )
            .bind("name", name)
            .bind("username", username)
            .bind("hashed_password", hashed_password)
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
            "select id, name, username, email, description, interests from dbo.Users where id = :id",
        )
            .bind("id", id)
            .mapTo(UserInfoRepo::class.java)
            .singleOrNull()
    }

    override fun getUserEvents(id: Int): List<SearchEventsOutputModel> {
        return handle.createQuery(
            """
        SELECT e.id, e.title, e.description, e.category, e.location, e.visibility, e.date
        FROM dbo.Event e
        JOIN dbo.UserParticipatesInEvent upe ON e.id = upe.event_id
        WHERE upe.user_id = :id
        """
        )
            .bind("id", id)
            .mapTo(SearchEventsOutputModel::class.java)
            .list()
    }

    override fun editUser(id: Int, name: String, description: String, interests: String) {
        handle.createUpdate(
            "update dbo.Users set name = :name, description = :description, interests = :interests where id = :id",
        )
            .bind("name", name)
            .bind("description", description)
            .bind("interests", interests)
            .bind("id", id)
            .execute()
    }

    override fun assignRole(userId: Int, roleName: String, eventId: Int): Int? =
        handle.createUpdate(
            "update dbo.Roles set name = :name where user_id = :user_id and event_id = :event_id",
        )
            .bind("name", roleName)
            .bind("event_id", eventId)
            .bind("user_id", userId)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()

    override fun removeRole(roleId: Int) {
        handle.createUpdate(
            "update dbo.Roles set name = :name where id = :role_id",
        )
            .bind("role_id", roleId)
            .bind("name", "Participant")
            .execute()

    }

    override fun getUserRole(userId: Int, eventId: Int): RoleOutputModel? {
        return handle.createQuery(
            "select id, name from dbo.Roles where user_id = :user_id and event_id = :event_id",
        )
            .bind("user_id", userId)
            .bind("event_id", eventId)
            .mapTo(RoleOutputModel::class.java)
            .singleOrNull()

    }

/*
    override fun uploadProfilePicture(id: Int, picture: ByteArray, fileType: String): Int? =
        handle.createUpdate(
            "update dbo.Users set profile_picture = :picture and profile_picture_type = :fileType where id = :id",
        )
            .bind("picture", picture)
            .bind("fileType", fileType)
            .bind("id", id)
            .execute()
 */

}