package ru.round.shave.dao

import ru.round.shave.entity.User

interface UserDao : CommonDao<User> {
    fun getAll(): List<User>
}