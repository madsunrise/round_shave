package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.round.shave.dao.WorkingHoursDao
import ru.round.shave.entity.WorkingHours
import ru.round.shave.service.WorkingHoursService
import java.time.LocalDate

@Service
@Transactional(rollbackFor = [Exception::class])
open class WorkingHoursServiceImpl : WorkingHoursService {

    @Autowired
    private lateinit var workingHoursDao: WorkingHoursDao

    override fun insert(entity: WorkingHours) {
        workingHoursDao.insert(entity)
    }

    override fun getAll(): List<WorkingHours> {
        return workingHoursDao.getAll()
    }

    override fun getWorkingHours(day: LocalDate): WorkingHours? {
        return workingHoursDao.getWorkingHours(day)
    }

    override fun getTheMostDistantWorkingHours(): WorkingHours? {
        return workingHoursDao.getTheMostDistantWorkingHours()
    }

    override fun deleteAllBefore(beforeInclusive: LocalDate): Int {
        return workingHoursDao.deleteAllBefore(beforeInclusive)
    }
}