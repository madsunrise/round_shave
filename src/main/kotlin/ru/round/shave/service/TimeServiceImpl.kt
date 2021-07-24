package ru.round.shave.service

import org.springframework.stereotype.Service
import ru.round.shave.RoundBot
import java.time.LocalDate
import java.time.LocalTime

@Service
class TimeServiceImpl : TimeService {
    override fun getDaysThatHaveFreeSlots(count: Int): List<LocalDate> {
        // пока считаем, что все ближайшие дни свободны
        val result = mutableListOf<LocalDate>()
        var current = LocalDate.now(RoundBot.TIME_ZONE)
        for (i in 0 until count) {
            result.add(current)
            current = current.plusDays(1L)
        }
        return result
    }

    override fun getFreeSlots(day: LocalDate): List<LocalTime> {
        val list = mutableListOf<LocalTime>()
        var current = LocalTime.of(11, 0)
        val max = LocalTime.of(20, 0)
        while (current < max) {
            list.add(current)
            current = current.plusMinutes(30L)
        }
        return list
    }
}