package ru.round.shave.service

import ru.round.shave.entity.WorkingHours
import java.time.LocalDate

interface WorkingHoursService {
    fun insert(entity: WorkingHours)

    fun getWorkingHours(day: LocalDate): WorkingHours?
}