package ru.round.shave.dao.impl

import org.springframework.stereotype.Repository
import ru.round.shave.dao.AppointmentDao
import ru.round.shave.entity.Appointment
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Predicate

@Repository
class AppointmentDaoImpl : AppointmentDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun insert(entity: Appointment) {
        em.persist(entity)
        em.flush()
    }

    override fun update(entity: Appointment) {
        em.merge(entity)
        em.flush()
    }

    override fun delete(entity: Appointment) {
        em.remove(if (em.contains(entity)) entity else em.merge(entity))
        em.flush()
    }

    override fun getById(id: Long): Appointment? {
        return em.find(Appointment::class.java, id)
    }

    override fun getAll(day: LocalDate, orderBy: AppointmentDao.OrderBy): List<Appointment> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(Appointment::class.java)
        val root = query.from(Appointment::class.java)

        val from = day.atStartOfDay()
        val to = from.plusDays(1L)

        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), from))
        predicates.add(cb.lessThan(root.get("endTime"), to))

        query.select(root)

        val afterOrderBy = when (orderBy) {
            AppointmentDao.OrderBy.TIME_ASC -> {
                query.orderBy(cb.asc(root.get<LocalDateTime>("startTime")))
            }
        }

        return em.createQuery(afterOrderBy).resultList
    }
}