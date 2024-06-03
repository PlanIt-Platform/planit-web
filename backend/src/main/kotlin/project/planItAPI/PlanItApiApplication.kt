package project.planItAPI

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlinx.datetime.Clock
import project.planItAPI.services.user.utils.UsersDomainConfig
import java.time.Duration

val jdbcUrl: String = "jdbc:postgresql://dpg-cpeuehnsc6pc73a4qc40-a.frankfurt-postgres.render.com:5432/planitapi?user=planitapi_user&password=fl4ErLyCceSq8UNyptFyBcNrsOj9vIhA"

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
