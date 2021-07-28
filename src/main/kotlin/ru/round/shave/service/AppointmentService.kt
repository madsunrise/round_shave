package ru.round.shave.service

import ru.round.shave.entity.Appointment
import java.time.LocalDate

interface AppointmentService : DatabaseService<Appointment> {
    fun getAll(day: LocalDate, orderBy: OrderBy): List<Appointment>

    enum class OrderBy {
        TIME_ASC
    }
}