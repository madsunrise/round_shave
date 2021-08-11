package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.round.shave.RoundBot
import ru.round.shave.service.AppointmentService
import ru.round.shave.service.TimeManagementService
import ru.round.shave.service.WorkingHoursService
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class TimeManagementServiceImpl : TimeManagementService {

    @Autowired
    private lateinit var workingHoursService: WorkingHoursService

    @Autowired
    private lateinit var appointmentService: AppointmentService

    override fun getDaysThatHaveFreeWindows(
        maxCount: Int,
        requiredDuration: Int,
        userCurrentTime: LocalDateTime
    ): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var current = LocalDate.now(RoundBot.TIME_ZONE)
        val maxPossibleDate = current.plusMonths(3L)
        while (result.size < maxCount && current < maxPossibleDate) {

            val hasFreeWindows = if (current == userCurrentTime.toLocalDate()) {
                // More complex logic because some windows can be in the past
                getFreeWindows(current, requiredDuration, userCurrentTime).isNotEmpty()
            } else {
                hasFreeWindows(day = current, requiredDuration = requiredDuration)
            }

            if (hasFreeWindows) {
                result.add(current)
            }

            current = current.plusDays(1L)
        }
        return result
    }

    override fun getFreeWindows(
        day: LocalDate,
        requiredDuration: Int,
        userCurrentTime: LocalDateTime
    ): List<LocalTime> {
        val windowsThatSatisfyUs = getAllFreeWindows(day).filter { it.getDurationInMinutes() >= requiredDuration }
        val sliced = windowsThatSatisfyUs.flatMap { it.sliceByHalfHour(windowMinDuration = requiredDuration) }
        val mapped = sliced.map { it.start }
        return if (day == userCurrentTime.toLocalDate()) {
            mapped.filter { it > userCurrentTime.toLocalTime() }
        } else {
            mapped
        }
    }

    private fun hasFreeWindows(day: LocalDate, requiredDuration: Int): Boolean {
        return getAllFreeWindows(day).any { it.getDurationInMinutes() >= requiredDuration }
    }

    private fun getAllFreeWindows(day: LocalDate): List<Window> {
        val workingHours = workingHoursService.getWorkingHours(day) ?: return emptyList()
        val allFreeWindows = mutableListOf<Window>()
        val appointments = appointmentService.getAll(day, AppointmentService.OrderBy.TIME_ASC)

        var windowStart = workingHours.startTime
        for (appointment in appointments) {
            val windowEnd = appointment.startTime.toLocalTime()
            if (windowEnd > windowStart) {
                allFreeWindows.add(Window(windowStart, windowEnd))
            }
            windowStart = appointment.endTime.toLocalTime()
        }

        val windowEnd = workingHours.endTime
        if (windowEnd > windowStart) {
            allFreeWindows.add(Window(windowStart, windowEnd))
        }

        return allFreeWindows
    }

    private data class Window(val start: LocalTime, val end: LocalTime) {
        init {
            require(end > start)
        }

        fun getDurationInMinutes(): Int {
            return Duration.between(start, end).toMinutes().toInt()
        }

        /**
         * Imagine that current window is (11:45, 13:55) and windowMinDuration = 20.
         * This method will return you next result:
         * [(11:45, 12:00), (12:00, 12:30), (12:30, 13:00), (13:00, 13:30), (13:30, 13:55)].
         *
         * Now imagine that current window is the same (11:45, 13:55) and windowMinDuration = 45.
         * This method will return you next result:
         * [(11:45, 12:00), (12:00, 12:30), (12:30, 13:00), (13:00, 13:55)]
         *
         * Finally, imagine that current window is (13:45, 14:30) and windowMinDuration = 45.
         * This method will return you next result: [(13:45, 14:30)]
         */
        fun sliceByHalfHour(windowMinDuration: Int): List<Window> {
            var first = start
            var second = minOf(first.appendToClosestHalf(), this.end)

            if (second == this.end) {
                return listOf(this)
            }

            if (second < this.end && Duration.between(second, this.end).toMinutes() < windowMinDuration) {
                // it's required to cover case with Window(13:45, 14:30) and windowMinDuration = 45
                return listOf(this)
            }

            val result = mutableListOf<Window>()
            while (first < second) {
                val window = Window(first, second)
                first = second
                second = minOf(first.appendToClosestHalf(), this.end)
                result.add(window)
                if (second < this.end && Duration.between(second, this.end).toMinutes() < windowMinDuration) {
                    val lastWindow = Window(window.end, this.end)
                    result.add(lastWindow)
                    break
                }
            }
            return result
        }

        /**
         * 12:00:00 -> 12:30:00
         * 12:00:59 -> 12:30:00
         * 12:05:00 -> 12:30:00
         * 12:20:00 -> 12:30:00
         * 12:30:00 -> 13:00:00
         * 12:40:00 -> 13:00:00
         * 12:59:59 -> 13:00:00
         */
        fun LocalTime.appendToClosestHalf(): LocalTime {
            val middle = LocalTime.of(hour, 30)
            return when {
                this < middle -> middle
                this == middle -> middle.plusMinutes(30L)
                else -> LocalTime.of(hour, 0).plusHours(1L)
            }
        }
    }
}