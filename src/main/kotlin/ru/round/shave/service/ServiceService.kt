package ru.round.shave.service

import ru.round.shave.entity.Service

interface ServiceService {
    fun getById(id: Long): Service?

    fun getAll(): List<Service>
}