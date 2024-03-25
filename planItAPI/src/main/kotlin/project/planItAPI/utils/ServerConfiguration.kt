package project.planItAPI.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for the server
 * @param accessTokenSecret the secret for the access token
 * @param refreshTokenSecret the secret for the refresh token
 * @param passwordSecret the secret for the password
 * @param tokenHashSecret the secret for the token hash
 */
@Configuration
class ServerConfiguration(
    @Value("\${server.config.secrets.access-token-secret}")
    val accessTokenSecret: String,

    @Value("\${server.config.secrets.refresh-token-secret}")
    val refreshTokenSecret: String,

    @Value("\${server.config.secrets.password-secret}")
    val passwordSecret: String,

    @Value("\${server.config.secrets.token-hash-secret}")
    val tokenHashSecret: String,
)