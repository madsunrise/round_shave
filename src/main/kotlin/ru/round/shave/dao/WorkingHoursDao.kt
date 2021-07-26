package ru.round.shave.dao

import ru.round.shave.entity.WorkingHours
import java.time.LocalDate

interface WorkingHoursDao {
    fun insert(entity: WorkingHours)

    fun getWorkingHours(day: LocalDate): WorkingHours?
}