package project.planItAPI.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import project.planItAPI.domain.event.Category
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.domain.event.Visibility
import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.utils.FakeEventServices
import project.planItAPI.services.utils.FakeTransactionManager
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreateEventException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserAlreadyInEventException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException
import kotlin.reflect.KClass

@SpringBootTest
class EventServicesTests {

    @MockBean
    private lateinit var usersRepository: UsersRepository

    @MockBean
    private lateinit var eventsRepository: EventsRepository

    private lateinit var eventsServices: FakeEventServices

    @BeforeEach
    fun setUp() {
        val fakeTransactionManager = FakeTransactionManager(usersRepository, eventsRepository)
        eventsServices = FakeEventServices(fakeTransactionManager)
    }

    private fun testCreateEvent(visibility: Visibility, password: String, expectedClass: KClass<*>, expectedId: Int? = null) {
        val category = (Category("Technology") as Success).value
        val subcategory = (Subcategory("Technology", "Web Development") as Success).value
        val date = (DateFormat("2024-12-12 12:00") as Success).value
        val endDate = (DateFormat("2024-12-24 15:00") as Success).value
        val money = (Money("100 EUR") as Success).value

        val eventResult = eventsServices.createEvent(
            "Event Title",
            "description",
            category,
            subcategory,
            "location",
            visibility,
            date,
            endDate,
            money,
            1,
            password
        )

        when (eventResult) {
            is Failure -> assertEquals(expectedClass, eventResult.value::class)
            is Success -> {
                assertEquals(expectedClass, eventResult.value::class)
                assertEquals(expectedId, eventResult.value.id)
            }
        }
    }

    private fun testEditEvent(userId: Int, eventId: Int, expectedClass: KClass<*>, expectedSuccessMessage: String? = null) {
        val category = (Category("Technology") as Success).value
        val subcategory = (Subcategory("Technology", "Web Development") as Success).value
        val date = (DateFormat("2024-12-12 12:00") as Success).value
        val endDate = (DateFormat("2024-12-24 15:00") as Success).value
        val money = (Money("100 EUR") as Success).value

        val eventResult = eventsServices.editEvent(
            userId,
            eventId,
            "Event Title",
            "description",
            category,
            subcategory,
            "location",
            Visibility.Public,
            date,
            endDate,
            money
        )

        when (eventResult) {
            is Failure -> assertEquals(expectedClass, eventResult.value::class)
            is Success -> {
                assertEquals(expectedClass, eventResult.value::class)
                assertEquals(expectedSuccessMessage, eventResult.value.success)
            }
        }
    }

    @Test
    fun `createEvent fails when visibility is private and password is blank`() {
        testCreateEvent(Visibility.Private, "", FailedToCreateEventException::class)
    }

    @Test
    fun `createEvent succeeds when visibility is private and password is not blank`() {
        testCreateEvent(Visibility.Private, "password", CreateEventOutputModel::class, 1)
    }

    @Test
    fun `createEvent succeeds when visibility is public`() {
        testCreateEvent(Visibility.Public, "", CreateEventOutputModel::class, 1)
    }

    @Test
    fun `getEvent fails when event is not found`() {
       val eventResult = eventsServices.getEvent(3)
        if (eventResult is Failure) {
            assertEquals(EventNotFoundException::class, eventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `getEvent succeeds when event is found`() {
        val eventResult = eventsServices.getEvent(1)
        if (eventResult is Success) {
            assertEquals(1, eventResult.value.id)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `getUsersInEvent fails when event is not found`() {
        val usersInEventResult = eventsServices.getUsersInEvent(3)
        if (usersInEventResult is Failure) {
            assertEquals(EventNotFoundException::class, usersInEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `getUsersInEvent succeeds when event is found`() {
        val usersInEventResult = eventsServices.getUsersInEvent(1)
        if (usersInEventResult is Success) {
            assertEquals(2, usersInEventResult.value.users.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `searchEvents succeeds`() {
        val searchEventsResult = eventsServices.searchEvents("All")
        if (searchEventsResult is Success) {
            assertEquals(2, searchEventsResult.value.events.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `searchEvents with input "Technology" succeeds`() {
        val searchEventsResult = eventsServices.searchEvents("Technology")
        if (searchEventsResult is Success) {
            assertEquals(1, searchEventsResult.value.events.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `searchEvents succeeds when the input doesn't match any event`() {
        val searchEventsResult = eventsServices.searchEvents("Not Found")
        if (searchEventsResult is Success) {
            assertEquals(0, searchEventsResult.value.events.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `searchEvents with empty input succeeds`() {
        val searchEventsResult = eventsServices.searchEvents("")
        if (searchEventsResult is Success) {
            assertEquals(2, searchEventsResult.value.events.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `joinEvent fails when event is not found`() {
        val joinEventResult = eventsServices.joinEvent(1, 3, "password")
        if (joinEventResult is Failure) {
            assertEquals(EventNotFoundException::class, joinEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `joinEvent fails when password is incorrect`() {
        val joinEventResult = eventsServices.joinEvent(1, 1, "incorrect")
        if (joinEventResult is Failure) {
            assertEquals(IncorrectPasswordException::class, joinEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `joinEvent fails when user is already in the event`() {
        val joinEventResult = eventsServices.joinEvent(2, 2, "123")
        if (joinEventResult is Failure) {
            assertEquals(UserAlreadyInEventException::class, joinEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `joinEvent succeeds`() {
        val joinEventResult = eventsServices.joinEvent(1, 2, "123")
        if (joinEventResult is Success) {
            assertEquals("User joined event with success.", joinEventResult.value.success)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `leaveEvent fails when event is not found`() {
        val leaveEventResult = eventsServices.leaveEvent(1, 3)
        if (leaveEventResult is Failure) {
            println(leaveEventResult)
            println(leaveEventResult.value::class)
            assertEquals(EventNotFoundException::class, leaveEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `leaveEvent fails when user is not in the event`() {
        val leaveEventResult = eventsServices.leaveEvent(2, 1)
        if (leaveEventResult is Failure) {
            assertEquals(UserNotInEventException::class, leaveEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `leaveEvent succeeds`() {
        val leaveEventResult = eventsServices.leaveEvent(1, 1)
        if (leaveEventResult is Success) {
            assertEquals("User left event with success.", leaveEventResult.value.success)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `deleteEvent fails when event is not found`() {
        val deleteEventResult = eventsServices.deleteEvent(1, 3)
        if (deleteEventResult is Failure) {
            assertEquals(EventNotFoundException::class, deleteEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `deleteEvent fails when user is not the organizer`() {
        val deleteEventResult = eventsServices.deleteEvent(2, 1)
        if (deleteEventResult is Failure) {
            assertEquals(UserIsNotOrganizerException::class, deleteEventResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `deleteEvent succeeds`() {
        val deleteEventResult = eventsServices.deleteEvent(1, 1)
        if (deleteEventResult is Success) {
            assertEquals("Event deleted with success.", deleteEventResult.value.success)
        } else {
            fail("Expected Success but got Failure")
        }
    }

    @Test
    fun `editEvent fails when event is not found`() {
        testEditEvent(1, 3, EventNotFoundException::class)
    }

    @Test
    fun `editEvent fails when user is not the organizer`() {
        testEditEvent(2, 1, UserIsNotOrganizerException::class)
    }

    @Test
    fun `editEvent succeeds`() {
        testEditEvent(1, 1, SuccessMessage::class, "Event edited with success.")
    }

    @Test
    fun `getSubcategories fails when category is not found`() {
        val subcategoriesResult = eventsServices.getSubcategories("Not Found")
        if (subcategoriesResult is Failure) {
            assertEquals(InvalidCategoryException::class, subcategoriesResult.value::class)
        } else {
            fail("Expected Failure but got Success")
        }
    }

    @Test
    fun `getSubcategories succeeds`() {
        val subcategoriesResult = eventsServices.getSubcategories("Technology")
        if (subcategoriesResult is Success) {
            assertEquals(7, subcategoriesResult.value.size)
        } else {
            fail("Expected Success but got Failure")
        }
    }
}