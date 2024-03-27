package project.planItAPI

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlinx.datetime.Clock
import project.planItAPI.repository.jdbi.utils.UsersDomainConfig
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration

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

	fun executeSQLScript() {
		val jdbcUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=123"

		val connection: Connection = DriverManager.getConnection(jdbcUrl)

		val script = String(Files.readAllBytes(Paths.get("backend/src/main/sql/createSchema.sql")))
		val statements = script.split(";")

		for (statement in statements) {
			connection.createStatement().execute(statement)
		}

		connection.close()
	}
}

fun main(args: Array<String>) {
	val application = PlanItApiApplication()
	application.executeSQLScript()
	runApplication<PlanItApiApplication>(*args)
}
