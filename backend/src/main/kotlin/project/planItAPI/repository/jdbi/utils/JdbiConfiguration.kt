package project.planItAPI.repository.jdbi.utils

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up the DataSource and Jdbi in the application.
 *
 * @property url The URL of the database.
 * @property username The username for connecting to the database.
 * @property password The password for connecting to the database.
 */
@Configuration
class JdbiConfiguration(
    @Value("\${spring.datasource.url}") val url: String,
    @Value("\${spring.datasource.username}") val username: String,
    @Value("\${spring.datasource.password}") val password: String
) {
    /**
     * Creates and configures a Jdbi bean with the DataSource settings.
     *
     * @return Configured Jdbi bean.
     */
    @Bean
    fun jdbi(): Jdbi {
        val dataSource = PGSimpleDataSource()
        dataSource.setURL(url)
        return Jdbi.create(dataSource)
            .configureWithAppRequirements()
    }
}