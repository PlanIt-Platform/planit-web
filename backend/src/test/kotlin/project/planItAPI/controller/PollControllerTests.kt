package project.planItAPI.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import project.planItAPI.services.event.EventServices
import project.planItAPI.services.poll.PollServices

@SpringBootTest
@AutoConfigureMockMvc
class PollControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var pollServices: PollServices

    @MockBean
    private lateinit var eventServices: EventServices
}