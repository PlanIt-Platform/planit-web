package project.planItAPI.controller

import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMethod
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserInfo
import project.planItAPI.models.UserLogInOutputModel
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.services.user.UserServices
import project.planItAPI.utils.Success

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userServices: UserServices

    private val name = "testUser"
    private val username = "testuser"
    private val email = "testuser@mail.com"
    private val password = "t3stP@ssword"

    private fun performRequest(
        method: RequestMethod,
        url: String,
        content: String,
        headers: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap()
    ): ResultActions {
        val requestBuilder = when (method) {
            RequestMethod.POST -> MockMvcRequestBuilders.post(url)
            RequestMethod.GET -> MockMvcRequestBuilders.get(url)
            RequestMethod.PUT -> MockMvcRequestBuilders.put(url)
            RequestMethod.DELETE -> MockMvcRequestBuilders.delete(url)
            else -> throw IllegalArgumentException("Unsupported request method: $method")
        }.apply {
            if(method != RequestMethod.GET) {
                contentType(MediaType.APPLICATION_JSON)
                content(content)
            }
            headers.forEach { (key, value) -> header(key, value) }
            cookies.forEach { (key, value) -> cookie(Cookie(key, value)) }
        }

        return mockMvc.perform(requestBuilder)
    }

    private fun authenticateUser(): String {

        `when`(userServices.register(any(), any(), any(), any()))
            .thenReturn(
                Success(
                    UserRegisterOutputModel(
                        1,
                        "testuser123",
                        "testUser",
                        "refreshToken",
                        "accessToken"
                    )
                )
            )

        val response = performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"testuser123\"," +
                    "\"name\":\"testUser\"," +
                    "\"email\":\"testuser@gmail.com\"," +
                    "\"password\":\"T3stP#ssword\"}")
            .andReturn().response

        return response.getCookie("access_token")?.value ?: ""
    }

    @Test
    fun register() {
        `when`(
            userServices.register(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Success(
                UserRegisterOutputModel(
                    id = 1,
                    name = name,
                    username = username,
                    refreshToken = "refreshToken",
                    accessToken = "accessToken"
                )
            )
        )

        //User registers successfully
        //Expected: 201 Created
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken"))

        //User logs in with an username that's too short
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"abc\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid username length"))

        //User registers without providing an username
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Username is blank"))

        //User registers with an invalid username
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"@bcdef\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid username"))

        //User registers with an invalid email
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"abc\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid email format"))

        //User registers with a password that is too short
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"Ab!1\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error")
                .value(
                "Password must follow the following parameters: " +
                        "Password must be at least 5 characters long"))

        //User register with a password that has no number
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"t!stPassword\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error")
                .value("Password must follow the following parameters: " +
                        "Password must contain at least one number"))

        //User registers with a password that has no uppercase letter
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"t!stpassword1\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error")
                .value("Password must follow the following parameters: " +
                        "Password must contain at least one uppercase letter"))

        //User registers with a password that has no special character
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/register",
            "{\"username\":\"${username}\"," +
                    "\"name\":\"${name}\"," +
                    "\"email\":\"${email}\"," +
                    "\"password\":\"testPassword1\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error")
                .value("Password must follow the following parameters: " +
                        "Password must contain at least one special character"))
    }

    @Test
    fun login() {
        `when`(
            userServices.login(
                any(),
                any()
            )
        ).thenReturn(
            Success(
                UserLogInOutputModel(
                    id = 1,
                    refreshToken = "refreshToken",
                    accessToken = "accessToken"
                )
            )
        )

        //User logs in successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.POST,
            "/api-planit/login",
            "{\"emailOrUsername\":\"${email}\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken"))

        //User logs in with an invalid email
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/login",
            "{\"emailOrUsername\":\"abc@gmail\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid email format"))

        //User logs in with an username that's too short
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/login",
            "{\"emailOrUsername\":\"abc\"," +
                    "\"password\":\"${password}\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid username length"))

        //User tries

        //User logs in with an invalid password
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/login",
            "{\"emailOrUsername\":\"${email}\"," +
                    "\"password\":\"Ab!1\"}")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error")
                .value(
                "Password must follow the following parameters: " +
                        "Password must be at least 5 characters long"))
    }

   @Test
   fun getUser(){
       `when`(
           userServices.getUser(
               any()
           )
       ).thenReturn(
           Success(
               UserInfo(
                     id = 1,
                     name = name,
                     username = username,
                     email = email,
                     description = "test description",
                     interests = listOf("test1", "test2")
                )
           )
       )

       `when`(
           userServices.register(
               any(),
               any(),
               any(),
               any()
           )
       ).thenReturn(
           Success(
               UserRegisterOutputModel(
                   id = 1,
                   name = name,
                   username = username,
                   refreshToken = "refreshToken",
                   accessToken = "accessToken"
               )
           )
       )

       performRequest(
           RequestMethod.POST,
           "/api-planit/register",
           "{\"username\":\"${username}\"," +
                   "\"name\":\"${name}\"," +
                   "\"email\":\"${email}\"," +
                   "\"password\":\"${password}\"}")

       //User gets his information successfully
       //Expected: 200 OK
       performRequest(
            RequestMethod.GET,
            "/api-planit/user/1",
            "",
            cookies = mapOf("access_token" to "accessToken")
       )
           .andExpect(status().isOk)
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.name").value(name))
           .andExpect(jsonPath("$.username").value(username))
           .andExpect(jsonPath("$.email").value(email))
           .andExpect(jsonPath("$.description").value("test description"))
           .andExpect(jsonPath("$.interests").isArray)
           .andExpect(jsonPath("$.interests[0]").value("test1"))
           .andExpect(jsonPath("$.interests[1]").value("test2"))

       //User tries to get his information without providing an access token
       //Expected: 401 Unauthorized
       performRequest(
            RequestMethod.GET,
            "/api-planit/user/1",
            ""
       )
           .andExpect(status().isUnauthorized)
           .andExpect(jsonPath("$.error").value("Unauthorized"))

       //User tries to get his information with an invalid ID
       //Expected: 400 Bad Request
       performRequest(
                RequestMethod.GET,
                "/api-planit/user/0",
                "",
                cookies = mapOf("access_token" to "accessToken")
       )
           .andExpect(status().isBadRequest)
           .andExpect(jsonPath("$.error").value("Invalid id"))
   }

    @Test
    fun editUser() {
        `when`(
            userServices.editUser(
                anyInt(),
                any(),
                anyString(),
                any()
            )
        ).thenReturn(
            Success(
                SuccessMessage("User information edited successfully")
            )
        )

        val accessToken = authenticateUser()

        //User edits his information successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.PUT,
            "/api-planit/user",
            "{\"name\":\"${name}\"," +
                    "\"description\":\"test description\"," +
                    "\"interests\":[\"Technology\",\"Business\"]}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value("User information edited successfully"))

        //User tries to edit his information without providing a name
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/user",
            "{\"name\":\"\"," +
                    "\"description\":\"test description\"," +
                    "\"interests\":[\"test1\",\"test2\"]}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Name is blank"))
    }
}