package ru.round.shave.dao

import ru.round.shave.entity.Appointment
import java.time.LocalDate

interface AppointmentDao : CommonDao<Appointment> {
    fun getAll(day: LocalDate, orderBy: OrderBy): List<Appointment>

    enum class OrderBy {
        TIME_ASC
    }
}