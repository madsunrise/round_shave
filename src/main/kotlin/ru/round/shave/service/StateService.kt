package ru.round.shave.service

import ru.round.shave.entity.Service
import ru.round.shave.entity.State
import ru.round.shave.entity.User
import java.time.LocalDate
import java.time.LocalTime

interface StateService {

    fun saveSelectedService(user: User, service: Service)

    fun saveSelectedDay(user: User, day: LocalDate)

    fun saveSelectedTime(user: User, time: LocalTime)

    fun getState(user: User): State?

    fun clearState(user: User)
}