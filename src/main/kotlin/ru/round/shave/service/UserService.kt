package ru.round.shave.service

import ru.round.shave.entity.User

interface UserService : DatabaseService<User> {
    fun getAll(): List<User>
}