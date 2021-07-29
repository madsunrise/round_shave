package ru.round.shave.service

import ru.round.shave.entity.User

interface UserService : DatabaseService<User> {
    fun getOrCreate(tgUser: com.github.kotlintelegrambot.entities.User): User

    fun getAll(): List<User>
}