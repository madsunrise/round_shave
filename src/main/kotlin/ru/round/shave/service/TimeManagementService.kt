package ru.round.shave.service

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface TimeManagementService {

    fun getDaysThatHaveFreeWindows(
        maxCount: Int,
        requiredDuration: Int,
        userCurrentTime: LocalDateTime
    ): List<LocalDate>

    /**
     * Calculates possible options for user and returns it. The result of this function
     * we can display to the user (e.g. 10:45, 11:00, 11:30, 13:00, 19:30)
     */
    fun getFreeWindows(day: LocalDate, requiredDuration: Int, userCurrentTime: LocalDateTime): List<LocalTime>
}