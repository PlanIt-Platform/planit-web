package project.planItAPI.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
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
import project.planItAPI.models.CreatePollOutputModel
import project.planItAPI.models.OptionVotesModel
import project.planItAPI.models.PollOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.services.poll.PollServices
import project.planItAPI.services.user.UserServices
import project.planItAPI.utils.Success

@SpringBootTest
@AutoConfigureMockMvc
class PollControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userServices: UserServices
    @MockBean
    private lateinit var pollServices: PollServices

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
    fun `create poll`() {
        val userId = authenticateUser()

        `when`(pollServices.createPoll(anyString(), anyList(), any(), anyInt(), anyInt()))
            .thenReturn(
                Success(
                    CreatePollOutputModel(
                        1,
                        "testPoll"
                    )
                )
            )

        //User tries to create a poll but has invalid duration
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/poll",
            "{\"title\":\"testPoll\",\"options\":[\"option1\",\"option2\"],\"duration\":0}",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid duration, must be one of the following values: 1 hour, 4 hours, 8 hours, 12 hours, 24 hours or 72 hours")
            )

        //User tries to create a poll but has invalid number of options
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/poll",
            "{\"title\":\"testPoll\",\"options\":[\"option1\"],\"duration\":4}",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid number of options, must be between 2 and 5")
            )

        val response = performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/poll",
            "{\"title\":\"testPoll\",\"options\":[\"option1\",\"option2\"],\"duration\":4}",
            cookies = mapOf("access_token" to userId)
        ).andReturn().response

        assert(response.status == 201)
        assert(response.contentAsString == ObjectMapper().writeValueAsString(CreatePollOutputModel(1, "testPoll")))
    }

    @Test
    fun getPoll() {
        val userId = authenticateUser()
        `when`(pollServices.getPoll(anyInt(), anyInt()))
            .thenReturn(
                Success(
                    PollOutputModel(
                        1,
                        "testPoll",
                        "2021-10-10T10:00:00",
                        4,
                        listOf(
                            OptionVotesModel(1, "option1", 0),
                            OptionVotesModel(2, "option2", 0)
                        )
                    )
                )
            )

        //User tries to get a poll but has invalid event id
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/0/poll/1",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        //User tries to get a poll but has invalid poll id
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/1/poll/0",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        val response = performRequest(
            RequestMethod.GET,
            "/api-planit/event/1/poll/1",
            "",
            cookies = mapOf("access_token" to userId)
        ).andReturn().response

        assert(response.status == 200)
        assert(response.contentAsString == ObjectMapper().writeValueAsString(
            PollOutputModel(
                1,
                "testPoll",
                "2021-10-10T10:00:00",
                4,
                listOf(
                    OptionVotesModel(1, "option1", 0),
                    OptionVotesModel(2, "option2", 0)
                )
            )
        ))
    }

    @Test
    fun deletePoll() {
        val userId = authenticateUser()
        `when`(pollServices.deletePoll(anyInt(), anyInt(), anyInt()))
            .thenReturn(
                Success(
                    SuccessMessage(
                        "Poll deleted successfully"
                    )
                )
            )

        //User tries to delete a poll but has invalid event id
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/0/poll/1",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        //User tries to delete a poll but has invalid poll id
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1/poll/0",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        val response = performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1/poll/1",
            "",
            cookies = mapOf("access_token" to userId)
        ).andReturn().response

        assert(response.status == 200)
        assert(response.contentAsString == ObjectMapper().writeValueAsString(SuccessMessage("Poll deleted successfully")))
    }

    @Test
    fun votePoll() {
        val userId = authenticateUser()
        `when`(pollServices.votePoll(anyInt(), anyInt(), anyInt(), anyInt()))
            .thenReturn(
                Success(
                    SuccessMessage(
                        "Vote registered successfully"
                    )
                )
            )

        //User tries to vote on a poll but has invalid event id
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/0/poll/1/vote/1",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        //User tries to vote on a poll but has invalid poll id
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/poll/0/vote/1",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        //User tries to vote on a poll but has invalid option id
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/poll/1/vote/0",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        val response = performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/poll/1/vote/1",
            "",
            cookies = mapOf("access_token" to userId)
        ).andReturn().response

        assert(response.status == 200)
        assert(response.contentAsString == ObjectMapper().writeValueAsString(SuccessMessage("Vote registered successfully")))
    }

    @Test
    fun getPolls() {
        val userId = authenticateUser()
        `when`(pollServices.getPolls(anyInt()))
            .thenReturn(
                Success(
                    listOf(
                        PollOutputModel(
                            1,
                            "testPoll",
                            "2021-10-10T10:00:00",
                            4,
                            listOf(
                                OptionVotesModel(1, "option1", 0),
                                OptionVotesModel(2, "option2", 0)
                            )
                        )
                    )
                )
            )

        //User tries to get polls but has invalid event id
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/0/polls",
            "",
            cookies = mapOf("access_token" to userId)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid id")
            )

        val response = performRequest(
            RequestMethod.GET,
            "/api-planit/event/1/polls",
            "",
            cookies = mapOf("access_token" to userId)
        ).andReturn().response

        assert(response.status == 200)
        assert(response.contentAsString == ObjectMapper().writeValueAsString(
            listOf(
                PollOutputModel(
                    1,
                    "testPoll",
                    "2021-10-10T10:00:00",
                    4,
                    listOf(
                        OptionVotesModel(1, "option1", 0),
                        OptionVotesModel(2, "option2", 0)
                    )
                )
            )
        ))
    }
}