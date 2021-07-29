package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.round.shave.dao.StateDao
import ru.round.shave.entity.Service
import ru.round.shave.entity.State
import ru.round.shave.entity.User
import ru.round.shave.service.StateService
import java.time.LocalDate
import java.time.LocalTime

@org.springframework.stereotype.Service
@Transactional(rollbackFor = [Exception::class])
open class StateServiceImpl : StateService {

    @Autowired
    private lateinit var stateDao: StateDao

    override fun createNewState(user: User): State {
        if (getUserState(user) != null) {
            throw IllegalStateException("User has already state, you must clear it before")
        }
        val state = State(user = user, currentStep = State.Step.INITIAL)
        stateDao.insert(state)
        return state
    }

    override fun getUserState(user: User): State? {
        return stateDao.getUserState(user)
    }

    override fun clearState(user: User) {
        stateDao.clearState(user)
    }

    override fun handleServiceChosen(state: State, service: Service): State {
        val updated = state.copy(
            service = service,
            day = null,
            time = null,
            currentStep = State.Step.SERVICE_CHOSEN
        )
        stateDao.update(updated)
        return updated
    }

    override fun handleDayChosen(state: State, day: LocalDate): State {
        val updated = state.copy(
            day = day,
            time = null,
            currentStep = State.Step.DAY_CHOSEN
        )
        stateDao.update(updated)
        return updated
    }

    override fun handleTimeChosen(state: State, time: LocalTime): State {
        val updated = state.copy(
            time = time,
            currentStep = State.Step.TIME_CHOSEN
        )
        stateDao.update(updated)
        return updated
    }

    override fun isFilled(state: State): Boolean {
        return state.service != null && state.day != null && state.time != null
    }
}


