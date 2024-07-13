package project.planItAPI.services.user.utils

import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.getNowTime
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserNotInEventException

fun validateRoleLogic(
    userId: Int,
    eventId: Int,
    organizerId: Int,
    usersRepository: UsersRepository,
    eventsRepository: EventsRepository
) {
    if (usersRepository.getUser(userId) == null) throw UserNotFoundException()
    val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
    if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
    val usersInEvent = eventsRepository.getUsersInEvent(eventId)
    if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) throw UserNotInEventException()
    if (organizerId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
}