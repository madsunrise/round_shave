package ru.round.shave.service

import java.time.LocalDate
import java.time.LocalTime

interface TimeService {
    // TODO а free slots это что? Если только отрезок 30 мин свободен за день, это считать?
    fun getDaysThatHaveFreeSlots(count: Int): List<LocalDate>

    fun getFreeSlots(day: LocalDate): List<LocalTime>
}