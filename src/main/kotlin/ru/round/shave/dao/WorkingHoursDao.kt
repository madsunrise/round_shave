package ru.round.shave.dao

import ru.round.shave.entity.WorkingHours
import java.time.LocalDate

interface WorkingHoursDao {
    fun insert(entity: WorkingHours)

    fun getAll(): List<WorkingHours>

    fun getWorkingHours(day: LocalDate): WorkingHours?

    fun getTheMostDistantWorkingHours(): WorkingHours?

    fun deleteAllBefore(beforeInclusive: LocalDate): Int

    fun delete(entity: WorkingHours)
}