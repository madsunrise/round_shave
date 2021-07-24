package ru.round.shave.service

import ru.round.shave.entity.Service
import ru.round.shave.entity.State
import ru.round.shave.entity.User
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

@org.springframework.stereotype.Service
class StateServiceImpl : StateService {
    private val inMemoryDatabase = ConcurrentHashMap<User, State>()

    override fun saveSelectedService(user: User, service: Service) {
        val newState = getOrCreate(user).copy(service = service)
        save(newState)
    }

    override fun saveSelectedDay(user: User, day: LocalDate) {
        val newState = getOrCreate(user).copy(day = day)
        save(newState)
    }

    override fun saveSelectedTime(user: User, time: LocalTime) {
        val newState = getOrCreate(user).copy(time = time)
        save(newState)
    }

    override fun getState(user: User): State? {
        return inMemoryDatabase[user]
    }

    override fun clearState(user: User) {
        inMemoryDatabase.remove(user)
    }

    private fun getOrCreate(user: User): State {
        if (inMemoryDatabase.containsKey(user)) {
            return inMemoryDatabase.getValue(user)
        }
        return State.empty(user).apply {
            inMemoryDatabase[user] = this
        }
    }

    private fun save(state: State) {
        inMemoryDatabase[state.user] = state
    }
}