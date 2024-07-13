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
import project.planItAPI.models.FindNearbyEventsListOutputModel
import project.planItAPI.models.FindNearbyEventsOutputModel
import project.planItAPI.models.JoinEventWithCodeOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.models.SearchEventsOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserInEvent
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.models.UsersInEventList
import project.planItAPI.services.event.EventServices
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserAlreadyInEventException

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
        locationType: String,
        location: String,
        latitude: Double,
        longitude: Double,
        visibility: String,
        date: String,
        endDate: String,
        price: String,
        password: String
    ): String {
        return "{\"title\":\"$title\",\"description\":\"$description\"," +
                "\"category\":\"$category\"," + "\"locationType\":\"$locationType\"," +
                "\"location\":\"$location\"," + "\"latitude\":$latitude," + "\"longitude\":$longitude," +
                "\"visibility\":\"$visibility\"," +
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
                any(),
                any(),
                any(),
                any(),
                anyString(),
                any(),
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
                    "AAAA",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,
                "Public",
                "2022-12-12 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,
                "Invalid Visibility", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
       )
           .andExpect(status().isBadRequest)
           .andExpect(jsonPath("$.error").value("Invalid visibility"))

        //User tries to create event with invalid category
        //Expected: 400 Bad Request
       performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Invalid Category",
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
           cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid category"))

        //User tries to create event with invalid date format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-40-40 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307, "Public", "2022-12-12 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12:00",
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
            eventServices.getEvent(1, 1))
            .thenReturn(
                Success(
                    EventOutputModel(
                        1,
                        "Test Event",
                        "Test Event Description",
                        "Business",
                        "Physical",
                        "Setubal",
                        38.5245,
                        -8.89307,
                        "Public",
                        "2022-12-12 12:00",
                        "2022-12-12 15:00",
                        10.52,
                      "Eur",
                        "AAAA"
                    )
                )
            )

        `when`(eventServices.getEvent(100, 1))
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
            .andExpect(jsonPath("$.location").value("Setubal"))
            .andExpect(jsonPath("$.visibility").value("Public"))
            .andExpect(jsonPath("$.date").value("2022-12-12 12:00"))
            .andExpect(jsonPath("$.endDate").value("2022-12-12 15:00"))
            .andExpect(jsonPath("$.priceAmount").value("10.52"))
            .andExpect(jsonPath("$.priceCurrency").value("Eur"))
    }

    @Test
    fun getUsersInEvent(){
        val usersInList = listOf(
            UserInEvent(
                1,
                "user1",
                1,
                "test123",
                "test"
            ),
            UserInEvent(
                2,
                "user2",
                2,
                "test1234",
                "test2"
            )
        )
        `when`(
            eventServices.getUsersInEvent(any(), any()))
            .thenReturn(Success(UsersInEventList(usersInList)))

        `when`(eventServices.getUsersInEvent(100, 1))
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
            eventServices.searchEvents("Test",10, 0))
            .thenReturn(
                Success(
                    SearchEventListOutputModel(
                        listOf(
                            SearchEventsOutputModel(
                                1,
                                "Test Event",
                                "Test Event Description",
                                "Business",
                                "Setubal",
                                38.5245,
                                -8.89307,
                                "Public",
                                "2022-12-12 12:00"
                            )
                        )
                    )
                )
            )

        `when`(
            eventServices.searchEvents("None", 10, 0))
            .thenReturn(
                Success(
                    SearchEventListOutputModel(
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
            .andExpect(jsonPath("$.events[0].category").value("Business"))
            .andExpect(jsonPath("$.events[0].location").value("Setubal"))
            .andExpect(jsonPath("$.events[0].visibility").value("Public"))
            .andExpect(jsonPath("$.events[0].date").value("2022-12-12 12:00"))

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
    fun findNearbyEvents(){
        `when`(
            eventServices.findNearbyEvents(anyInt(), any(), anyInt(), anyInt()))
            .thenReturn(
                Success(
                    FindNearbyEventsListOutputModel(
                        listOf(
                           FindNearbyEventsOutputModel(
                                 1,
                                 "Test Event",
                                 "Setubal",
                                 38.5245,
                                 -8.89307,
                            )
                           )
                        )
                    )
                )

        //User not authenticated tries to search for nearby events
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.GET,
            "/api-planit/events/50/30.0/-8.0",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to search for nearby events successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.GET,
            "/api-planit/events/50/38.519400/-9.013800",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.events", Matchers.hasSize<Any>(1)))
            .andExpect(jsonPath("$.events[0].id").value(1))
            .andExpect(jsonPath("$.events[0].title").value("Test Event"))
            .andExpect(jsonPath("$.events[0].location").value("Setubal"))
            .andExpect(jsonPath("$.events[0].latitude").value(38.5245))
            .andExpect(jsonPath("$.events[0].longitude").value(-8.89307))
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
            .thenReturn(Failure(InvalidValueException("Id")))

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
            .andExpect(jsonPath("$.error").value("Invalid id"))

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
    fun joinEventByCode() {
        `when`(
            eventServices.joinEventByCode(1, "AAAAAA"))
            .thenReturn(
                Success(
                  JoinEventWithCodeOutputModel(
                      "Test Event",
                        1,
                        "User joined event with success."
                    )
                  )
                )

        `when`(eventServices.joinEventByCode(1, "BBBBBB"))
            .thenReturn(Failure(EventNotFoundException()))

        `when`(eventServices.joinEventByCode(1, "CCCCCC"))
            .thenReturn(Failure(EventHasEndedException()))

        `when`(eventServices.joinEventByCode(1, "DDDDDD"))
            .thenReturn(Failure(UserAlreadyInEventException()))

        //User not authenticated tries to join an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/AAAAAA",
            ""
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to join an event with an invalid event code
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/123",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid event code"))


        //User tries to join an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/BBBBBB",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to join an event that has ended
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/CCCCCC",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Event has ended"))

        //User tries to join an event that he is already in
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/DDDDDD",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("You are already in the event"))

        //User tries to join an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.POST,
            "/api-planit/event/AAAAAA",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Event"))
            .andExpect(jsonPath("$.message").value("User joined event with success."))
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
            .thenReturn(Failure(InvalidValueException("Id")))

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
            .andExpect(jsonPath("$.error").value("Invalid id"))

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
            .thenReturn(Failure(InvalidValueException("Id")))

        //User not authenticated tries to delete an Event
        //Expected: 401 Unauthorized
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1",
            "",
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))

        val accessToken = authenticateUser()

        //User tries to delete an event with an invalid event id
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/0",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid id"))

        //User tries to delete an event that does not exist
        //Expected: 404 Not Found
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/100",
            "",
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Event not found"))

        //User tries to delete an event successfully
        //Expected: 200 OK
        performRequest(
            RequestMethod.DELETE,
            "/api-planit/event/1",
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
                any(),
                any(),
                any(),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyString()
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307, "Public", "2022-12-12 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307, "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00 EUR", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid id"))

        //User tries to edit an event with invalid visibility
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Simple Meeting",
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Invalid Visibility", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid visibility"))

        //User tries to create event with invalid category
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Invalid Category",
                "Physical",
                "Setubal",
                38.5245,
                -8.89307, "Public", "2022-12-12 12:00",
                "2022-12-12 15:00", "10.00E", ""
            ),
            cookies = mapOf("access_token" to accessToken)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid category"))

        //User tries to create event with invalid date format
        //Expected: 400 Bad Request
        performRequest(
            RequestMethod.PUT,
            "/api-planit/event/1/edit",
            createEventBody(
                "Test Event", "Test Event Description", "Business",
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12:00",
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
                "Physical",
                "Setubal",
                38.5245,
                -8.89307,"Public", "2022-12-12 12:00",
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
                "Physical", "Setúbal", 38.5245, -8.89307,
                "Public", "2022-12-12 12:00",
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
}



