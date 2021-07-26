package ru.round.shave.dao

import ru.round.shave.exception.AlreadyExistException

interface CommonDao<T> {
    @Throws(AlreadyExistException::class)
    fun insert(entity: T)
    fun update(entity: T)
    fun delete(entity: T)
    fun getById(id: Long): T?
}