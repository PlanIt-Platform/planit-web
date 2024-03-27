package project.planItAPI.repository.jdbi.utils

/**
 * Data class representing configuration parameters for the user domain.
 *
 * @property tokenSizeInBytes The size of the tokens in bytes.
 * @property refreshTokenTTL The time-to-live duration for refresh tokens.
 * @property accessTokenTTL The time-to-live duration for access tokens.
 * @property maxTokensPerUser The maximum number of tokens allowed per user.
 *
 * @throws IllegalArgumentException if any of the configuration parameters are invalid.
 */
data class UsersDomainConfig(
    val tokenSizeInBytes: Int,
    val refreshTokenTTL: java.time.Duration,
    val accessTokenTTL: java.time.Duration,
    val maxTokensPerUser: Int
) {
    init {
        require(tokenSizeInBytes > 0)
        require(!refreshTokenTTL.isNegative)
        require(!accessTokenTTL.isNegative)
        require(maxTokensPerUser > 0)
    }
}