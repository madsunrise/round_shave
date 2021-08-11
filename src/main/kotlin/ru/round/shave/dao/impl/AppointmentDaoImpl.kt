package ru.round.shave.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.round.shave.dao.AppointmentDao
import ru.round.shave.entity.Appointment
import ru.round.shave.entity.User
import ru.round.shave.exception.AlreadyExistException
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
        if (!checkIsTimeFree(entity.startTime, entity.endTime)) {
            LOGGER.warn("Can't insert appointment: time is taken! $entity")
            throw AlreadyExistException("Time is already taken!")
        }
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

        query
            .select(root)
            .where(*predicates.toTypedArray())

        val afterOrderBy = when (orderBy) {
            AppointmentDao.OrderBy.TIME_ASC -> {
                query.orderBy(cb.asc(root.get<LocalDateTime>("startTime")))
            }
        }

        return em.createQuery(afterOrderBy).resultList
    }

    override fun getAppointmentsInPast(
        currentTime: LocalDateTime,
        user: User,
        orderBy: AppointmentDao.OrderBy
    ): List<Appointment> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(Appointment::class.java)
        val root = query.from(Appointment::class.java)

        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.lessThan(root.get("endTime"), currentTime))
        predicates.add(cb.equal(root.get<User>("user"), user))

        query
            .select(root)
            .where(*predicates.toTypedArray())

        val afterOrderBy = when (orderBy) {
            AppointmentDao.OrderBy.TIME_ASC -> {
                query.orderBy(cb.asc(root.get<LocalDateTime>("startTime")))
            }
        }

        return em.createQuery(afterOrderBy).resultList
    }

    override fun getAppointmentsInFuture(
        currentTime: LocalDateTime,
        user: User,
        orderBy: AppointmentDao.OrderBy
    ): List<Appointment> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(Appointment::class.java)
        val root = query.from(Appointment::class.java)

        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.greaterThan(root.get("startTime"), currentTime))
        predicates.add(cb.equal(root.get<User>("user"), user))

        query
            .select(root)
            .where(*predicates.toTypedArray())

        val afterOrderBy = when (orderBy) {
            AppointmentDao.OrderBy.TIME_ASC -> {
                query.orderBy(cb.asc(root.get<LocalDateTime>("startTime")))
            }
        }

        return em.createQuery(afterOrderBy).resultList
    }

    private fun checkIsTimeFree(start: LocalDateTime, end: LocalDateTime): Boolean {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(Appointment::class.java)
        val root = query.from(Appointment::class.java)

        val predicates = mutableListOf<Predicate>()
        predicates.add(
            cb.or(
                cb.and(
                    cb.greaterThanOrEqualTo(root.get("startTime"), start),
                    cb.lessThan(root.get("startTime"), end)
                ),
                cb.and(
                    cb.greaterThan(root.get("endTime"), start),
                    cb.lessThanOrEqualTo(root.get("endTime"), end)
                ),
                cb.and(
                    cb.lessThan(root.get("startTime"), start),
                    cb.greaterThan(root.get("endTime"), end)
                )
            )
        )

        query
            .select(root)
            .where(*predicates.toTypedArray())

        return em.createQuery(query).resultList.isEmpty()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AppointmentDaoImpl::class.java.simpleName)
    }
}