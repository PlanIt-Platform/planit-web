package project.planItAPI.services

import project.planItAPI.repository.jdbi.event.EventsRepository
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserNotInEventException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun generateEventCode(): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return List(6) { chars.random() }.joinToString("")
}

fun getNowTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return LocalDateTime.now().format(formatter)
}

fun dateToMilliseconds(date: String): Long {
    val dotIndex = date.indexOf('.')
    val dateStringWithoutFractionalSeconds = date.substring(0, dotIndex)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(dateStringWithoutFractionalSeconds, formatter)
    return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
}