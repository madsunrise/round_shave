package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.round.shave.dao.ServiceDao
import ru.round.shave.entity.Service
import ru.round.shave.service.ServiceService

@org.springframework.stereotype.Service
@Transactional(rollbackFor = [Exception::class])
open class ServiceServiceImpl : ServiceService {

    @Autowired
    private lateinit var serviceDao: ServiceDao

    override fun getById(id: Long): Service? {
        return serviceDao.getById(id)
    }

    override fun getAll(): List<Service> {
        return serviceDao.getAll()
    }
}