package ru.round.shave.service

import ru.round.shave.entity.WorkingHours
import java.time.LocalDate

interface WorkingHoursService {
    fun insert(entity: WorkingHours)

    fun getAll(): List<WorkingHours>

    fun getWorkingHours(day: LocalDate): WorkingHours?

    fun getTheMostDistantWorkingHours(): WorkingHours?

    fun deleteAllBefore(beforeInclusive: LocalDate): Int

    fun delete(entity: WorkingHours)
}