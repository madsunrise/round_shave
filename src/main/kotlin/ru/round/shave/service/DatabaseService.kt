package ru.round.shave.service

import ru.round.shave.exception.AlreadyExistException

interface DatabaseService<T> {
    @Throws(AlreadyExistException::class)
    fun insert(entity: T)
    fun update(entity: T)
    fun delete(entity: T)
    fun getById(id: Long): T?
}
