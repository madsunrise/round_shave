package ru.round.shave.dao

import ru.round.shave.entity.User

interface UserDao : CommonDao<User> {
    fun getOrCreate(tgUser: com.github.kotlintelegrambot.entities.User, chatId: Long): User

    fun getAll(): List<User>
}