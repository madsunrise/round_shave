package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.round.shave.dao.AppointmentDao
import ru.round.shave.entity.Appointment
import ru.round.shave.service.AppointmentService
import java.time.LocalDate

@Service
@Transactional(rollbackFor = [Exception::class])
open class AppointmentServiceImpl : AppointmentService {

    @Autowired
    private lateinit var appointmentDao: AppointmentDao

    override fun insert(entity: Appointment) {
        appointmentDao.insert(entity)
    }

    override fun update(entity: Appointment) {
        appointmentDao.update(entity)
    }

    override fun delete(entity: Appointment) {
        appointmentDao.delete(entity)
    }

    override fun getById(id: Long): Appointment? {
        return appointmentDao.getById(id)
    }

    override fun getAll(day: LocalDate): List<Appointment> {
        return appointmentDao.getAll(day)
    }
}