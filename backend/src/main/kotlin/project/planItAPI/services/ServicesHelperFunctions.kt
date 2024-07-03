package project.planItAPI.services

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.*

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

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Radius of the earth in km
    val dLat = deg2rad(lat2 - lat1)
    val dLon = deg2rad(lon2 - lon1)
    val a = sin(dLat/2) * sin(dLat/2) +
            cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
            sin(dLon/2) * sin(dLon/2)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
    // Distance in meters
    return r * c * 1000
}

fun deg2rad(deg: Double): Double {
    return deg * (PI/180)
}