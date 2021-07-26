package ru.round.shave.dao

import ru.round.shave.entity.Service

interface ServiceDao {
    fun getById(id: Long): Service?

    fun getAll(): List<Service>
}