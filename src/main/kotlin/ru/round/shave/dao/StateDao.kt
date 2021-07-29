package ru.round.shave.dao

import ru.round.shave.entity.State
import ru.round.shave.entity.User

interface StateDao : CommonDao<State> {
    fun clearState(user: User)

    fun getUserState(user: User): State?
}