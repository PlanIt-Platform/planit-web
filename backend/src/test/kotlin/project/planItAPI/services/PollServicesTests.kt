package project.planItAPI.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import project.planItAPI.domain.poll.Option
import project.planItAPI.domain.poll.TimeFormat
import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.poll.PollRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.utils.FakePollServices
import project.planItAPI.services.utils.FakeTransactionManager
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.OptionNotFoundException
import project.planItAPI.utils.PollNotFoundException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserAlreadyVotedException
import project.planItAPI.utils.UserIsNotOrganizerException

@SpringBootTest
class PollServicesTests {

    @MockBean
    private lateinit var usersRepository: UsersRepository

    @MockBean
    private lateinit var eventsRepository: EventsRepository
    @MockBean
    private lateinit var pollsRepository: PollRepository

    private lateinit var pollServices: FakePollServices

    @BeforeEach
    fun setUp() {
        val fakeTransactionManager = FakeTransactionManager(usersRepository, eventsRepository, pollsRepository)
        pollServices = FakePollServices(fakeTransactionManager)
    }

    @Test
    fun `createPoll should return a Success message when the poll is created successfully`() {
        // Arrange
        val title = "Poll Title"
        val opt1 = (Option("Option 1") as Success).value
        val opt2 = (Option("Option 2") as Success).value
        val options = listOf(opt1, opt2)
        val duration = (TimeFormat("1") as Success).value
        val organizerId = 1
        val eventId = 1

        // Act
        val result = pollServices.createPoll(title, options, duration, organizerId, eventId)

        // Assert
        if (result is Success) {
            assert(result.value.id == 1)
            assert(result.value.title == title)
        } else {
            assert(false)
        }
    }

    @Test
    fun `createPoll should return a Failure message when the event is not found`() {
        // Arrange
        val title = "Poll Title"
        val opt1 = (Option("Option 1") as Success).value
        val opt2 = (Option("Option 2") as Success).value
        val options = listOf(opt1, opt2)
        val duration = (TimeFormat("1") as Success).value
        val organizerId = 1
        val eventId = -1

        // Act
        val result = pollServices.createPoll(title, options, duration, organizerId, eventId)

        // Assert
        if (result is Failure){
            assert(EventNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Event not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `createPoll should return a Failure message when the user is not the organizer`() {
        // Arrange
        val title = "Poll Title"
        val opt1 = (Option("Option 1") as Success).value
        val opt2 = (Option("Option 2") as Success).value
        val options = listOf(opt1, opt2)
        val duration = (TimeFormat("1") as Success).value
        val organizerId = 2
        val eventId = 1

        // Act
        val result = pollServices.createPoll(title, options, duration, organizerId, eventId)

        // Assert
        if (result is Failure){
            assert(UserIsNotOrganizerException()::class.java.isInstance(result.value))
            assert(result.value.message == "You are not the organizer of the event")
        } else {
            assert(false)
        }
    }

    @Test
    fun `getPoll should return a Success message when the poll is found`() {
        // Arrange
        val pollId = 1
        val eventId = 1

        // Act
        val result = pollServices.getPoll(pollId, eventId)

        // Assert
        if (result is Success) {
            assert(result.value.id == 1)
            assert(result.value.title == "Sample Poll")
        } else {
            assert(false)
        }
    }

    @Test
    fun `getPoll should return a Failure message when the event is not found`() {
        // Arrange
        val pollId = 1
        val eventId = -1

        // Act
        val result = pollServices.getPoll(pollId, eventId)

        // Assert
        if (result is Failure){
            assert(EventNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Event not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `getPoll should return a Failure message when the poll is not found`() {
        // Arrange
        val pollId = 2
        val eventId = 1

        // Act
        val result = pollServices.getPoll(pollId, eventId)

        // Assert
        if (result is Failure){
            assert(PollNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Poll not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `deletePoll should return a Success message when the poll is deleted successfully`() {
        // Arrange
        val pollId = 1
        val eventId = 1
        val userId = 1

        // Act
        val result = pollServices.deletePoll(userId, pollId, eventId)

        // Assert
        if (result is Success) {
            assert(result.value.success == "Poll deleted successfully")
        } else {
            assert(false)
        }
    }

    @Test
    fun `deletePoll should return a Failure message when the event is not found`() {
        // Arrange
        val pollId = 1
        val eventId = -1
        val userId = 1

        // Act
        val result = pollServices.deletePoll(userId, pollId, eventId)

        // Assert
        if (result is Failure){
            assert(EventNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Event not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `deletePoll should return a Failure message when the user is not the organizer`() {
        // Arrange
        val pollId = 1
        val eventId = 1
        val userId = 2

        // Act
        val result = pollServices.deletePoll(userId, pollId, eventId)

        // Assert
        if (result is Failure){
            assert(UserIsNotOrganizerException()::class.java.isInstance(result.value))
            assert(result.value.message == "You are not the organizer of the event")
        } else {
            assert(false)
        }
    }

    @Test
    fun `deletePoll should return a Failure message when the poll is not found`() {
        // Arrange
        val pollId = 2
        val eventId = 1
        val userId = 1

        // Act
        val result = pollServices.deletePoll(userId, pollId, eventId)

        // Assert
        if (result is Failure){
            assert(PollNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Poll not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `votePoll should return a Success message when the vote is registered successfully`() {
        // Arrange
        val pollId = 1
        val eventId = 1
        val userId = 2
        val optionId = 1

        // Act
        val result = pollServices.votePoll(userId, pollId, optionId, eventId)

        // Assert
        if (result is Success) {
            assert(result.value.success == "Vote registered successfully")
        } else {
            assert(false)
        }
    }

    @Test
    fun `votePoll should return a Failure message when the event is not found`() {
        // Arrange
        val pollId = 1
        val eventId = -1
        val userId = 2
        val optionId = 1

        // Act
        val result = pollServices.votePoll(userId, pollId, optionId, eventId)

        // Assert
        if (result is Failure){
            assert(EventNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Event not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `votePoll should return a Failure message when the poll is not found`() {
        // Arrange
        val pollId = 2
        val eventId = 1
        val userId = 2
        val optionId = 1

        // Act
        val result = pollServices.votePoll(userId, pollId, optionId, eventId)

        // Assert
        if (result is Failure){
            assert(PollNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Poll not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `votePoll should return a Failure message when the option is not found`() {
        // Arrange
        val pollId = 1
        val eventId = 1
        val userId = 2
        val optionId = 2

        // Act
        val result = pollServices.votePoll(userId, pollId, optionId, eventId)

        // Assert
        if (result is Failure){
            assert(OptionNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Option not found")
        } else {
            assert(false)
        }
    }

    @Test
    fun `votePoll should return a Failure message when the user has already voted`() {
        // Arrange
        val pollId = 1
        val eventId = 1
        val userId = 1
        val optionId = 1

        // Act
        val result = pollServices.votePoll(userId, pollId, optionId, eventId)

        // Assert
        if (result is Failure){
            assert(UserAlreadyVotedException()::class.java.isInstance(result.value))
            assert(result.value.message == "You have already voted")
        } else {
            assert(false)
        }
    }

    @Test
    fun `getPolls should return a Success message when the polls are found`() {
        // Arrange
        val eventId = 1

        // Act
        val result = pollServices.getPolls(eventId)

        // Assert
        if (result is Success) {
            assert(result.value.size == 2)
            assert(result.value[0].id == 1)
            assert(result.value[0].title == "Sample Poll")
            assert(result.value[1].id == 2)
            assert(result.value[1].title == "Another Poll")
        } else {
            assert(false)
        }
    }

    @Test
    fun `getPolls should return a Failure message when the event is not found`() {
        // Arrange
        val eventId = -1

        // Act
        val result = pollServices.getPolls(eventId)

        // Assert
        if (result is Failure){
            assert(EventNotFoundException()::class.java.isInstance(result.value))
            assert(result.value.message == "Event not found")
        } else {
            assert(false)
        }
    }
}