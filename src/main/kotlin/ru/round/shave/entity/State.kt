package ru.round.shave.entity

import java.time.LocalDate
import java.time.LocalTime

data class State(
    val user: User,
    val service: Service? = null,
    val day: LocalDate? = null,
    val time: LocalTime? = null
) {
    fun isFull(): Boolean {
        return service != null && day != null && time != null
    }

    companion object {
        fun empty(user: User) = State(user = user, service = null, day = null, time = null)
    }
}