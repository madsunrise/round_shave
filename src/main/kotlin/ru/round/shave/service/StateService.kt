package ru.round.shave.service

import ru.round.shave.entity.Service
import ru.round.shave.entity.State
import ru.round.shave.entity.User
import java.time.LocalDate
import java.time.LocalTime

interface StateService {
    fun createNewState(user: User): State

    fun getUserState(user: User): State?

    fun clearState(user: User)

    fun handleServiceChosen(state: State, service: Service): State

    fun handleDayChosen(state: State, day: LocalDate): State

    fun handleTimeChosen(state: State, time: LocalTime): State

    fun isFilled(state: State): Boolean
}