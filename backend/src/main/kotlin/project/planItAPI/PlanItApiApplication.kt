package project.planItAPI

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlinx.datetime.Clock
import project.planItAPI.services.user.utils.UsersDomainConfig
import java.time.Duration

val jdbcUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=123"

private const val createSchemaScriptPath = "sql/createSchema.sql"

@SpringBootApplication
class PlanItApiApplication{
	@Bean
	fun usersDomainConfig() = UsersDomainConfig(
		tokenSizeInBytes = 512 / 8,
		refreshTokenTTL = Duration.ofDays(1),
		accessTokenTTL = Duration.ofHours(1),
		maxTokensPerUser = 3
	)

	@Bean
	fun clock() = Clock.System
}

fun main(args: Array<String>) {
	PlanItApiApplication()
	executeSQLScript(jdbcUrl, createSchemaScriptPath)
	runApplication<PlanItApiApplication>(*args)
}
