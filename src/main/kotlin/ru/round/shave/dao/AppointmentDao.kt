package ru.round.shave.dao

import ru.round.shave.entity.Appointment
import ru.round.shave.entity.User
import java.time.LocalDate
import java.time.LocalDateTime

interface AppointmentDao : CommonDao<Appointment> {
    fun getAll(day: LocalDate, orderBy: OrderBy): List<Appointment>

    fun getByStartTime(startTime: LocalDateTime): Appointment?

    fun getAppointmentsInPast(currentTime: LocalDateTime, user: User?, orderBy: OrderBy): List<Appointment>

    fun getAppointmentsInFuture(currentTime: LocalDateTime, user: User?, orderBy: OrderBy): List<Appointment>

    enum class OrderBy {
        TIME_ASC
    }
}