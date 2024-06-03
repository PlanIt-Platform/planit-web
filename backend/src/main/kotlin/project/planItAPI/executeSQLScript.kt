package project.planItAPI

import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

fun executeSQLScript(jdbcUrl: String, scriptPath: String) {
    val resourceLoader: ResourceLoader = PathMatchingResourcePatternResolver()
    val resource = resourceLoader.getResource("classpath:$scriptPath")

    val connection: Connection = DriverManager.getConnection(jdbcUrl)

    val script = BufferedReader(InputStreamReader(resource.inputStream)).readText()
    val statements = script.split(";")

    for (statement in statements) {
        connection.createStatement().execute(statement)
    }

    connection.close()
}

