package project.planItAPI.repository

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import project.planItAPI.domain.event.Money
import project.planItAPI.executeSQLScript
import project.planItAPI.repository.jdbi.event.JdbiEventsRepository
import project.planItAPI.repository.jdbi.poll.JdbiPollRepository
import project.planItAPI.repository.jdbi.user.JdbiUsersRepository
import project.planItAPI.repository.jdbi.utils.configureWithAppRequirements
import project.planItAPI.utils.Success
import java.sql.Timestamp

class PollsRepositoryTests {
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
    fun tearDown() {
        // Clean the database after each test.
        // This script will delete all the data from the users table.
        executeSQLScript(JDBI_URL, EVENT_SCRIPT_PATH)
    }

    @Test
    fun `createPoll should return a poll ID`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll("Sample Poll", emptyList(), 1, eventId!!, organizerId!!)
            assertEquals(1, pollId)
        }
    }

    @Test
    fun `getPoll should return a poll`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll("Sample Poll", emptyList(), 1, eventId!!, organizerId!!)
            val poll = pollRepository.getPoll(pollId)
            assertEquals("Sample Poll", poll?.title)
        }
    }

    @Test
    fun `getPoll should return null if the poll does not exist`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val poll = pollRepository.getPoll(1)
            assertEquals(null, poll)
        }
    }

    @Test
    fun `getPoll should return the poll options`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll(
                "Sample Poll",
                listOf("Option 1", "Option 2"),
                1,
                eventId!!,
                organizerId!!
            )
            val poll = pollRepository.getPoll(pollId)
            assertEquals(2, poll?.options?.size)
        }
    }

    @Test
    fun `getPoll should return the poll votes`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll(
                "Sample Poll",
                listOf("Option 1", "Option 2"),
                1,
                eventId!!,
                organizerId!!
            )
            pollRepository.vote(pollId, organizerId, 1)
            val user2Id = usersRepository.register("Name", "Name2", "email2@gmail.com", "ABC")
            pollRepository.vote(pollId, user2Id!!, 2)
            val user3Id = usersRepository.register("Name", "Name3", "email3@gmail.com", "ABC")
            pollRepository.vote(pollId, user3Id!!, 1)

            val poll = pollRepository.getPoll(pollId)
            assertEquals(2, poll?.options?.firstOrNull { it.id == 1 }?.votes)
            assertEquals(1, poll?.options?.firstOrNull { it.id == 2 }?.votes)
        }
    }

    @Test
    fun `deletePoll should delete a poll`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll("Sample Poll", emptyList(), 1, eventId!!, organizerId!!)
            pollRepository.deletePoll(pollId)
            val poll = pollRepository.getPoll(pollId)
            assertEquals(null, poll)
        }
    }

    @Test
    fun `deletePoll should delete the poll options`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll(
                "Sample Poll",
                listOf("Option 1", "Option 2"),
                1,
                eventId!!,
                organizerId!!
            )
            pollRepository.deletePoll(pollId)
            val options = handle.createQuery("SELECT * FROM dbo.Options WHERE poll_id = :pollId")
                .bind("pollId", pollId)
                .mapToMap()
                .list()
            assertEquals(0, options.size)
        }
    }

    @Test
    fun `vote should add a vote`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll(
                "Sample Poll",
                listOf("Option 1", "Option 2"),
                1,
                eventId!!,
                organizerId!!
            )
            pollRepository.vote(pollId, 1, 1)
            val votes = handle.createQuery("SELECT * FROM dbo.UserVotes WHERE poll_id = :pollId")
                .bind("pollId", pollId)
                .mapToMap()
                .list()
            assertEquals(1, votes.size)
        }
    }

    @Test
    fun `vote should not add a vote if the user already voted`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            val pollId = pollRepository.createPoll(
                "Sample Poll",
                listOf("Option 1", "Option 2"),
                1,
                eventId!!,
                organizerId!!
            )
            handle.createUpdate("INSERT INTO dbo.UserVotes (user_id, option_id, poll_id) VALUES (1, 1, :poll_id)")
                .bind("poll_id", pollId)
            pollRepository.vote(pollId, 1, 1)
            val votes = handle.createQuery("SELECT * FROM dbo.UserVotes WHERE poll_id = :pollId")
                .bind("pollId", pollId)
                .mapToMap()
                .list()
            assertEquals(1, votes.size)
        }
    }

    @Test
    fun `getPolls should return all the polls`() {
        runWithHandle { handle ->
            val pollRepository = JdbiPollRepository(handle)
            val eventRepository = JdbiEventsRepository(handle)
            val usersRepository = JdbiUsersRepository(handle)
            val organizerId = usersRepository.register("Name", "Name", "email@gmail.com", "ABC")
            val money = (Money("20.0 USD") as Success).value
            val eventId = eventRepository.createEvent(
                "Sample Event",
                "Description",
                "Technology",
                "Online",
                "Zoom",
                10.0,
                10.0,
                "Public",
                Timestamp(System.currentTimeMillis()),
                null,
                money,
                1,
                "",
                "AAAA"
            )
            pollRepository.createPoll("Sample Poll 1", emptyList(), 1, eventId!!, organizerId!!)
            pollRepository.createPoll("Sample Poll 2", emptyList(), 1, eventId, organizerId)
            val polls = pollRepository.getPolls(eventId)
            assertEquals(2, polls.size)
        }
    }
}