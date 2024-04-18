package project.planItAPI

import java.io.File
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

fun executeSQLScript(jdbcUrl: String, scriptPath: String) {
    val rootPath = Paths.get("").toAbsolutePath().toString() // Get project root path
    val fullPath = Paths.get(rootPath, scriptPath).toString() // Resolve full path

    val connection: Connection = DriverManager.getConnection(jdbcUrl)

    val script = File(fullPath).readText()
    val statements = script.split(";")

    for (statement in statements) {
        connection.createStatement().execute(statement)
    }

    connection.close()
}

