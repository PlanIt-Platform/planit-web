package project.planItAPI.repository

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import project.planItAPI.executeSQLScript
import project.planItAPI.repository.jdbi.event.JdbiEventsRepository
import project.planItAPI.repository.jdbi.user.JdbiUsersRepository
import project.planItAPI.repository.jdbi.utils.configureWithAppRequirements
import java.sql.Timestamp

class EventsRepositoryTests {
    companion object {
        private const val JDBI_URL = "jdbc:postgresql://localhost:5432/PlanItTestDatabase?user=postgres&password=123"
        private const val EVENT_SCRIPT_PATH = "sql/createEvent.sql"

        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(JDBI_URL)
            }
        ).configureWithAppRequirements()
    }

    @BeforeEach
    fun tearDown(){
        // Clean the database after each test.
        // This script will delete all the data from the users table.
        executeSQLScript(JDBI_URL, EVENT_SCRIPT_PATH)
    }

    private fun registerUser(username: String, password: String, email: String, name: String): Int {
        var userId: Int? = null
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)
            userId = repo.register(name, username, email, password)
        }
        return userId!!
    }

    @Test
    fun `Create event is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            assert(eventID != null)

            val event = repo.getEvent(eventID!!)

            assert(event != null)
            if (event != null) {
                assertEquals(event.id, eventID)
                assertEquals(event.title, "title")
                assertEquals(event.description, "description")
                assertEquals(event.category, "Technology")
                assertEquals(event.subcategory, "Web Development")
                assertEquals(event.location, "location")
            }

            //Check if user is in the event
            val userInEvent = repo.getUsersInEvent(eventID)
            assert(userInEvent != null)
            assertEquals(userInEvent!!.users.size, 1)
            assertEquals(userInEvent.users[0].id, userId)

            //Check if user is Organizer
            val userOrganizers = repo.getEventOrganizers(eventID)
            assertEquals(userOrganizers[0], userId)
        }
    }

    @Test
    fun `getEvent returns null when event does not exist`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val event = repo.getEvent(1)
            assert(event == null)
        }
    }

    @Test
    fun `getEvent returns event when event exists`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            assert(eventID != null)

            val event = repo.getEvent(eventID!!)
            assert(event != null)
        }
    }

    @Test
    fun `getUsersInEvent returns emptyList when event does not exist`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val usersList = repo.getUsersInEvent(1)
            assert(usersList!!.users.isEmpty())
        }
    }

    @Test
    fun `getUsersInEvent returns users when event exists`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = null,
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            assert(eventID != null)

            // Register second user and join event
            val userId2 = registerUser("user123", "password1", "user2@gmail.com", "User2")
            repo.joinEvent(2, eventID!!)

            val users = repo.getUsersInEvent(eventID)
            assert(users != null)
            assertEquals(users!!.users.size, 2)
            assertEquals(users.users[0].id, userId)
            assertEquals(users.users[1].id, userId2)
        }
    }

    @Test
    fun `searchEvent returns emptyList when no events are found`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val eventList = repo.searchEvents("Event", 5, 5)
            assert(eventList.events.isEmpty())
        }
    }

    @Test
    fun `searchEvent returns events when events are found`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            val eventList = repo.searchEvents("title", 10, 0)

            assert(eventList.events.isNotEmpty())
            assert(eventList.events.size == 1)
            assertEquals(eventList.events[0].id, eventID)

            val secondEventID = repo.createEvent(
                title = "WebDev",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            val secondEventList = repo.searchEvents("WebDev", 10, 0)

            assert(secondEventList.events.isNotEmpty())
            assert(secondEventList.events.size == 1)
            assertEquals(secondEventList.events[0].id, secondEventID)

            val allEventsList = repo.searchEvents("Technology",10 ,0)

            assert(allEventsList.events.isNotEmpty())
            assert(allEventsList.events.size == 2)
            assertEquals(allEventsList.events[0].id, eventID)
            assertEquals(allEventsList.events[1].id, secondEventID)
        }
    }

    @Test
    fun `getAllEvents returns emptyList when no events are found`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val eventList = repo.getAllEvents(5, 5)
            assert(eventList.events.isEmpty())
        }
    }

    @Test
    fun `getAllEvents returns events when events are found`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )
            val secondEventID = repo.createEvent(
                title = "WebDev",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            val eventList = repo.getAllEvents(10, 0)

            assert(eventList.events.isNotEmpty())
            assert(eventList.events.size == 2)
            assertEquals(eventList.events[0].id, eventID)
            assertEquals(eventList.events[1].id, secondEventID)
        }
    }

    @Test
    fun `joinEvent is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = null,
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            val userId2 = registerUser("user123", "password1", "user2@gmail.com", "User2")
            repo.joinEvent(2, eventID!!)

            val users = repo.getUsersInEvent(eventID)
            assert(users != null)
            assertEquals(users!!.users.size, 2)
            assertEquals(users.users[0].id, userId)
            assertEquals(users.users[1].id, userId2)
        }
    }

    @Test
    fun `leaveEvent is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = null,
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            registerUser("user123", "password1", "user2@gmail.com", "User2")
            repo.joinEvent(2, eventID!!)


            repo.kickUserFromEvent(2, eventID)

            val users = repo.getUsersInEvent(eventID)
            assertEquals(users!!.users.size, 1)
            assertEquals(users.users[0].id, userId)
        }
    }

    @Test
    fun `deleteEvent is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = null,
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            if (eventID != null) {
                repo.deleteEvent(eventID)
                val event = repo.getEvent(eventID)
                assert(event == null)
            }
        }
    }

    @Test
    fun `editEvent is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            repo.editEvent(
                eventId = eventID!!,
                title = "newTitle",
                description = "newDescription",
                category = "newCategory",
                subcategory = "newSubcategory",
                location = "newLocation",
                visibility = "Private",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                password = ""
            )

            val event = repo.getEvent(eventID)
            assert(event != null)
            if (event != null) {
                assertEquals(event.title, "newTitle")
                assertEquals(event.description, "newDescription")
                assertEquals(event.category, "newCategory")
                assertEquals(event.subcategory, "newSubcategory")
                assertEquals(event.location, "newLocation")
                assertEquals(event.visibility, "Private")
            }
        }
    }

    @Test
    fun `getEventOrganizer is successful`() {
        runWithHandle { handle ->
            val repo = JdbiEventsRepository(handle)
            val userId = registerUser("user", "password", "user@gmail.com", "User")
            val eventID = repo.createEvent(
                title = "title",
                description = "description",
                category = "Technology",
                subcategory = "Web Development",
                location = "location",
                visibility = "Public",
                date = Timestamp(System.currentTimeMillis()),
                end_date = null,
                price = null,
                userID = userId,
                password = ""
            )

            val organizers = repo.getEventOrganizers(eventID!!)
            assertEquals(organizers[0], userId)
        }
    }
}