package project.planItAPI.repository.jdbi.utils.users


import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.stereotype.Component
import project.planItAPI.utils.PasswordException
import project.planItAPI.utils.MultiplePasswordExceptions
import project.planItAPI.utils.PasswordHasNoNumber
import project.planItAPI.utils.PasswordHasNoSpecialChar
import project.planItAPI.utils.PasswordHasNoUppercase
import project.planItAPI.utils.PasswordTooShort
import project.planItAPI.utils.ServerConfiguration
import java.security.SignatureException
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Business logic and domain operations related to user management.
 *
 * @property config Configuration parameters for user domain operations.
 * @property serverConfig Configuration parameters for server-related secrets.
 * @property accessTokenKey Secret key for signing access tokens.
 * @property refreshTokenKey Secret key for signing refresh tokens.
 * @property tokenKey Secret key for general token validation.
 * @property passwordKey Secret key for creating password validation.
 */
@Component
class UsersDomain(
    private val config: UsersDomainConfig,
    serverConfig: ServerConfiguration
) {

    /**
     * Secret key for signing access tokens.
     */
    private val accessTokenKey = SecretKeySpec(
        /* key = */ serverConfig.accessTokenSecret.toByteArray(),
        /* algorithm = */ "HmacSHA512"
    )

    /**
     * Secret key for signing refresh tokens.
     */
    private val refreshTokenKey = SecretKeySpec(
        /* key = */ serverConfig.refreshTokenSecret.toByteArray(),
        /* algorithm = */ "HmacSHA512"
    )

    /**
     * Secret key for general token validation.
     */
    private val tokenKey: SecretKey = SecretKeySpec(
        /* key = */ serverConfig.tokenHashSecret.toByteArray(),
        /* algorithm = */ "HmacSHA512"
    )

    /**
     * Secret key for creating password validation.
     */
    private val passwordKey: SecretKey = SecretKeySpec(
        /* key = */ serverConfig.passwordSecret.toByteArray(),
        /* algorithm = */ "HmacSHA512"
    )

    /**
     * Data class representing the details of a refresh token.
     *
     * @property token The refresh token string.
     * @property expirationDate The expiration date of the refresh token.
     */
    data class RefreshTokenDetails(
        val token: String,
        val expirationDate: Timestamp
    )

    /**
     * Creates an access token for the specified username.
     *
     * @param username The username for which the access token is created.
     * @return The generated access token.
     */
    fun createAccessToken(username: String): String {
        val issuedAt = Instant.now()
        val expirationDate = issuedAt.plus(config.accessTokenTTL)

        val claims = Jwts.claims()
        claims["username"] = username

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date.from(issuedAt))
            .setExpiration(Date.from(expirationDate))
            .signWith(accessTokenKey)
            .compact()
    }

    /**
     * Creates a refresh token and its details for the specified username.
     *
     * @param username The username for which the refresh token is created.
     * @return The details of the generated refresh token.
     */
    fun createRefreshToken(username: String): RefreshTokenDetails {
        val issuedAt = Instant.now()
        val expirationDate = issuedAt.plus(config.refreshTokenTTL)

        val claims = Jwts.claims()
        claims["username"] = username

        return RefreshTokenDetails(
            token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expirationDate))
                .signWith(refreshTokenKey)
                .compact(),
            expirationDate = Timestamp.from(expirationDate)
        )
    }

    /**
     * Validates a password against a hashed password.
     *
     * @param string The password to validate.
     * @param hashedPassword The hashed password for comparison.
     * @return `true` if the password is valid, `false` otherwise.
     */
    fun validatePassword(string: String, hashedPassword: String) =
        createHashedPassword(string) == hashedPassword

    /**
     * Creates a hashed password using the specified password.
     *
     * @param password The password to hash.
     * @return The hashed password.
     */
    fun createHashedPassword(password: String): String = password.hmac(passwordKey)

    /**
     * Creates a hashed token using the specified token.
     *
     * @param token The token to hash.
     * @return The hashed token.
     */
    fun createTokenValidation(token: String): String = token.hmac(tokenKey)

    /**
     * Checks the validity of an access token.
     *
     * @param token The access token to validate.
     * @return `true` if the token is valid, `false` otherwise.
     */
    fun checkAccessToken(token: String): Boolean = isTokenValid(token, accessTokenKey)

    /**
     * Checks the validity of a refresh token.
     *
     * @param token The refresh token to validate.
     * @return `true` if the token is valid, `false` otherwise.
     */
    fun checkRefreshToken(token: String): Boolean = isTokenValid(token, refreshTokenKey)

    /**
     * Checks if a token is valid using the specified token key.
     *
     * @param token The token to check.
     * @param tokenKey The secret key for token validation.
     * @return `true` if the token is valid, `false` otherwise.
     */
    fun isTokenValid(token: String, tokenKey: SecretKeySpec): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(tokenKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: SignatureException) {
            false
        } catch (e: MalformedJwtException) {
            false
        } catch (e: UnsupportedJwtException) {
            false
        }
    }

    /**
     * Retrieves the username associated with an access token.
     *
     * @param token The access token to extract the username from.
     * @return The username associated with the access token, or `null` if not valid.
     */
    fun getUserByToken(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(accessTokenKey)
                .build()
                .parseClaimsJws(token)
                .body

            return claims["username"] as String?
        } catch (e: ExpiredJwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: SignatureException) {
            null
        } catch (e: MalformedJwtException) {
            null
        } catch (e: UnsupportedJwtException) {
            null
        }
    }

    /**
     * Computes the HMAC (Hash-based Message Authentication Code) of a string using the specified key.
     *
     * @param key The secret key for HMAC.
     * @return The computed HMAC as a hexadecimal string.
     */
    private fun String.hmac(key: SecretKey): String =
        Mac.getInstance("HmacSHA512")
            .also { it.init(key) }
            .doFinal(this.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }

    /**
     * Checks if a password is considered safe.
     *
     * @param password The password to check.
     * @return `true` if the password is considered safe, `false` otherwise.
     */
    fun isPasswordSafe(password: String){
        val exceptions = mutableListOf<PasswordException>()

        if (password.length < 5) {
            exceptions.add(PasswordTooShort())
        }
        if (!password.any { it.isDigit() }) {
            exceptions.add(PasswordHasNoNumber())
        }
        if (!password.any { it.isUpperCase() }) {
            exceptions.add(PasswordHasNoUppercase())
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            exceptions.add(PasswordHasNoSpecialChar())
        }

        if (exceptions.isNotEmpty()) {
            throw MultiplePasswordExceptions(exceptions)
        }
    }
}
