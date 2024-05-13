package project.planItAPI.controller

import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMethod
import project.planItAPI.services.user.UserServices
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.models.EventOutputModel
import project.planItAPI.models.SearchEventOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserInEvent
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.models.UsersInEventList
import project.planItAPI.services.event.EventServices
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.InvalidIdException
import project.planItAPI.utils.Success

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userServices: UserServices
    @MockBean
    private lateinit var eventServices: EventServices

    private fun performRequest(
        method: RequestMethod,
        url: String,
        content: String,
        headers: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap()
    ): ResultActions {
        val requestBuilder = when (method) {
            RequestMethod.POST -> post(url)
            RequestMethod.GET -> get(url)
            RequestMethod.PUT -> put(url)
            RequestMethod.DELETE -> delete(url)
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

    fun createEventBody(
        title: String,
        description: String,
        category: String,
        subCategory: String,
        location: String,
        visibility: String,
        date: String,
        endDate: String,
        price: String,
        password: String
    ): String {
        return "{\"title\":\"$title\",\"description\":\"$description\"," +
                "\"category\":\"$category\",\"subCategory\":\"$subCategory\"," +
                "\"location\":\"$location\",\"visibility\":\"$visibility\"," +
                "\"date\":\"$date\",\"endDate\":\"$endDate\"," +
                "\"price\":\"$price\",\"password\":\"$password\"}"
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
    fun createEvent() {
        `when`(
            eventServices.createEvent(
                anyString(),
                anyString(),
                any(),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyString()
            )
        ).thenReturn(
            Success(
                CreateEventOutputModel(
                    1,
                    "Test Event",
                    "Created with success."
                )
            )
        )

        //User not authenticated tries to create an Event
        //Expected: 401 Unauthorized
       performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Simple Meeting",
                "", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            )
       )
           .andExpect(status().isUnauthorized)
           .andExpect(jsonPath("$.error").value("Unauthorized"))

       val accessToken = authenticateUser()

        //User tries to create event with invalid visibility
        //Expected: 400 Bad Request
       performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Simple Meeting",
                "", "Test Location", "Invalid Visibility", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
       )
           .andExpect(status().isBadRequest)
           .andExpect(jsonPath("$.error").value("Invalid visibility value"))

        //User tries to create event with invalid category
        //Expected: 400 Bad Request
       performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Invalid Category",
                "", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
           cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid category, Invalid subcategory"))

        //User tries to create event with invalid subcategory
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Invalid Subcategory", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid subcategory"))

        //User tries to create event with invalid date format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("2022-12-12 12 has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'")
            )

        //User tries to create event with invalid month/day format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-40-40 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("2022-40-40 12:00 has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'")
            )

        //User tries to create event with invalid endDate format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("2022-12-12 15 has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'")
            )

        //User tries to create event with invalid price format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid price format. The correct format is 'amount currency'. Example: '10.00 USD'")
            )

        //User creates an event successfully
        //Expected: 201 Created
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Event"))
            .andExpect(jsonPath("$.status").value("Created with success."))
    }

    @Test
    fun getEvent(){
        `when`(
            eventServices.getEvent(1))
            .thenReturn(
                Success(
                    EventOutputModel(
                        1,
                        "Test Event",
                        "Test Event Description",
                        "Business",
                        "Leadership and Management Workshops",
                        "Test Location",
                        "Public",
                        "2022-12-12 12:00",
                        "2022-12-12 15:00",
                        10.52,
                      "Eur",
                      ""
                    )
                )
            )

        `when`(eventServices.getEvent(100))
            .thenReturn(Failure(EventNotFoundException()))

        //User not authenticated tries to get an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/1",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to get an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/100",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to get an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/1",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Event"))
            .andExpect(jsonPath("$.description").value("Test Event Description"))
            .andExpect(jsonPath("$.category").value("Business"))
            .andExpect(jsonPath("$.subcategory").value("Leadership and Management Workshops"))
            .andExpect(jsonPath("$.location").value("Test Location"))
            .andExpect(jsonPath("$.visibility").value("Public"))
            .andExpect(jsonPath("$.date").value("2022-12-12 12:00"))
            .andExpect(jsonPath("$.endDate").value("2022-12-12 15:00"))
            .andExpect(jsonPath("$.priceAmount").value("10.52"))
            .andExpect(jsonPath("$.priceCurrency").value("Eur"))
            .andExpect(jsonPath("$.password").isEmpty)
    }

    @Test
    fun getUsersInEvent(){
        val usersInList = listOf(
            UserInEvent(
                1,
                "user1",
                "user1"
            ),
            UserInEvent(
                2,
                "user2",
                "user2"
            )
        )
        `when`(
            eventServices.getUsersInEvent(any()))
            .thenReturn(Success(UsersInEventList(usersInList)))

        `when`(eventServices.getUsersInEvent(100))
            .thenReturn(Failure(EventNotFoundException()))

        //User not authenticated tries to get users in an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/1/users",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to get users in an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/100/users",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to get users in an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/1/users",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.users", Matchers.hasSize<Any>(2)))
    }

    @Test
    fun searchEvents(){
        `when`(
            eventServices.searchEvents("Test"))
            .thenReturn(
                Success(
                    SearchEventOutputModel(
                        listOf(
                            EventOutputModel(
                                1,
                                "Test Event",
                                "Test Event Description",
                                "Business",
                                "Leadership and Management Workshops",
                                "Test Location",
                                "Public",
                                "2022-12-12 12:00",
                                "2022-12-12 15:00",
                                10.52,
                                "Eur",
                                ""
                            )
                        )
                    )
                )
            )

        `when`(
            eventServices.searchEvents("None"))
            .thenReturn(
                Success(
                    SearchEventOutputModel(
                        emptyList()
                    )
                )
            )

        //User not authenticated tries to search for events
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.GET,
            "/api-planit/events?searchInput=Test",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to search for events successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/events?searchInput=Test",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.events", Matchers.hasSize<Any>(1)))
            .andExpect(jsonPath("$.events[0].id").value(1))
            .andExpect(jsonPath("$.events[0].title").value("Test Event"))
            .andExpect(jsonPath("$.events[0].description").value("Test Event Description"))
            .andExpect(jsonPath("$.events[0].category").value("Business"))
            .andExpect(jsonPath("$.events[0].subcategory").value("Leadership and Management Workshops"))
            .andExpect(jsonPath("$.events[0].location").value("Test Location"))
            .andExpect(jsonPath("$.events[0].visibility").value("Public"))
            .andExpect(jsonPath("$.events[0].date").value("2022-12-12 12:00"))
            .andExpect(jsonPath("$.events[0].endDate").value("2022-12-12 15:00"))
            .andExpect(jsonPath("$.events[0].priceAmount").value("10.52"))
            .andExpect(jsonPath("$.events[0].priceCurrency").value("Eur"))
            .andExpect(jsonPath("$.events[0].password").isEmpty)

        performRequest(
            RequestMethod.GET,
            "/api-planit/events?searchInput=None",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.events", Matchers.hasSize<Any>(0)))
    }

    @Test
    fun joinEvent() {
        `when`(
            eventServices.joinEvent(1, 1, ""))
            .thenReturn(
                Success(
                    SuccessMessage(
                    "User joined event with success."
                    )
                )
            )

        `when`(eventServices.joinEvent(1, 100, ""))
            .thenReturn(Failure(EventNotFoundException()))

        `when`(eventServices.joinEvent(1, 0, ""))
            .thenReturn(Failure(InvalidIdException()))

        `when`(eventServices.joinEvent(1, 1, "123"))
            .thenReturn(Failure(IncorrectPasswordException()))

        //User not authenticated tries to join an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/join",
            "{\"password\":\"\"}",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to join an event with an invalid event id
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/0/join",
            "{\"password\":\"\"}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid ID"))

        //User tries to join an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/100/join",
            "{\"password\":\"\"}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to join an event with an invalid password
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/join",
            "{\"password\":\"123\"}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Incorrect password"))

        //User tries to join an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/join",
            "{\"password\":\"\"}",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value("User joined event with success."))
    }

    @Test
    fun leaveEvent() {
        `when`(
            eventServices.leaveEvent(1, 1))
            .thenReturn(
                Success(
                    SuccessMessage(
                        "User left event with success."
                    )
                )
            )

        `when`(eventServices.leaveEvent(1, 100))
            .thenReturn(Failure(EventNotFoundException()))

        `when`(eventServices.leaveEvent(1, 0))
            .thenReturn(Failure(InvalidIdException()))

        //User not authenticated tries to leave an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/leave",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to leave an event with an invalid event id
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/0/leave",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid ID"))

        //User tries to leave an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/100/leave",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to leave an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/1/leave",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value("User left event with success."))
    }

    @Test
    fun deleteEvent() {
        `when`(
            eventServices.deleteEvent(1, 1))
            .thenReturn(
                Success(
                    SuccessMessage(
                        "Event deleted with success."
                    )
                )
            )

        `when`(eventServices.deleteEvent(1, 100))
            .thenReturn(Failure(EventNotFoundException()))

        `when`(eventServices.deleteEvent(1, 0))
            .thenReturn(Failure(InvalidIdException()))

        //User not authenticated tries to delete an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1/delete",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to delete an event with an invalid event id
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/0/delete",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid ID"))

        //User tries to delete an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/100/delete",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to delete an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1/delete",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value("Event deleted with success."))
    }

    @Test
    fun editEvent() {
        `when`(
            eventServices.editEvent(
                anyInt(),
                anyInt(),
                anyString(),
                anyString(),
                any(),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Success(
                SuccessMessage(
                    "Event edited with success."
                )
            )
        )



        //User not authenticated tries to edit an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to edit an event with an invalid event id
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/0/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid ID"))

        //User tries to edit an event with invalid visibility
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Simple Meeting",
                "", "Test Location", "Invalid Visibility", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid visibility value"))

        //User tries to create event with invalid category
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Invalid Category",
                "", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid category, Invalid subcategory"))

        //User tries to create event with invalid subcategory
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Invalid Subcategory", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid subcategory"))

        //User tries to create event with invalid date format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("2022-12-12 12 has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'")
            )

        //User tries to create event with invalid endDate format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("2022-12-12 15 has invalid timestamp. The correct format is 'YYYY-MM-DD HH:MM'")
            )

        //User tries to create event with invalid price format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.error")
                    .value("Invalid price format. The correct format is 'amount currency'. Example: '10.00 USD'")
            )


        //User tries to edit an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Leadership and Management Workshops", "Test Location", "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value("Event edited with success."))
    }

    @Test
    fun getCategories() {
        `when`(
            eventServices.getCategories())
            .thenReturn(
                Success(
                    listOf(
                        "Category 1",
                        "Category 2",
                        "Category 3"
                    )
                )
            )

        val accessToken = authenticateUser()

        //User tries to get categories successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/categories",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Any>(3)))
    }

    @Test
    fun getSubcategories() {
        `when`(
            eventServices.getSubcategories("Technology"))
            .thenReturn(
                Success(
                    listOf(
                        "Web Development",
                        "Mobile Development",
                        "Software Development",
                        "Networking and Security",
                        "Artificial Intelligence and Machine Learning",
                        "Tech Startups and Entrepreneurship",
                        "Emerging Technologies"
                    )
                )
            )

        `when`(
            eventServices.getSubcategories("Invalid Category"))
            .thenReturn(
                Failure(InvalidCategoryException())
            )

        val accessToken = authenticateUser()

        //User tries to get subcategories successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/categories/Technology/subcategories",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Any>(7)))

        //User tries to get subcategories for an invalid category
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.GET,
            "/api-planit/event/categories/InvalidCategory/subcategories",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid category"))
    }
}



