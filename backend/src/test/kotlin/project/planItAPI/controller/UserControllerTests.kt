package project.planItAPI.controller

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import project.planItAPI.executeSQLScript
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {

    @LocalServerPort
    var port: Int = 0

    private val client: HttpClient = HttpClient.newHttpClient()

    private val name = "testUser"
    private val username = "testuser"
    private val email = "testuser@mail.com"
    private val password = "t3stP@ssword"

    @Nested
    inner class RegisterTest {
        @Test
        fun `can register a new user`() {

            val body1 = HttpRequest.BodyPublishers.ofString(
                "{\"name\":\"$name\",\"username\":\"$username\",\"email\":\"$email\",\"password\":\"$password\"}",
            )
            val registerResponse1 = client.send(
                HttpRequest
                    .newBuilder()
                    .uri(URI("http://localhost:$port/api-planit/register"))
                    .POST(body1)
                    .headers("Content-Type", "application/json")
                    .build(),
                HttpResponse.BodyHandlers.ofString(),
            )

            assertEquals(201, registerResponse1.statusCode())
            assertTrue(registerResponse1.headers().firstValue("access_token") != null)
            assertTrue(registerResponse1.headers().firstValue("refresh_token") != null)


        }
    }


}