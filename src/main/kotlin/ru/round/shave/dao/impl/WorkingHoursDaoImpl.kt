package ru.round.shave.dao.impl

import org.springframework.stereotype.Repository
import ru.round.shave.dao.WorkingHoursDao
import ru.round.shave.entity.WorkingHours
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Predicate

@Repository
class WorkingHoursDaoImpl : WorkingHoursDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun insert(entity: WorkingHours) {
        em.persist(entity)
        em.flush()
    }

    override fun getAll(): List<WorkingHours> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(WorkingHours::class.java)
        val root = query.from(WorkingHours::class.java)

        query
            .select(root)
            .orderBy(cb.asc(root.get<LocalDate>("day")))

        return em.createQuery(query).resultList
    }

    override fun getWorkingHours(day: LocalDate): WorkingHours? {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(WorkingHours::class.java)
        val root = query.from(WorkingHours::class.java)

        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.equal(root.get<LocalDate>("day"), day))

        query
            .select(root)
            .where(*predicates.toTypedArray())

        return em.createQuery(query).resultList.firstOrNull()
    }

    override fun getTheMostDistantWorkingHours(): WorkingHours? {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(WorkingHours::class.java)
        val root = query.from(WorkingHours::class.java)

        query
            .select(root)
            .orderBy(cb.desc(root.get<LocalDate>("day")))

        return em.createQuery(query).resultList.firstOrNull()
    }

    override fun deleteAllBefore(beforeInclusive: LocalDate): Int {
        val cb = em.criteriaBuilder
        val query = cb.createCriteriaDelete(WorkingHours::class.java)
        val root = query.from(WorkingHours::class.java)
        query.where(cb.lessThanOrEqualTo(root.get("day"), beforeInclusive))
        return em.createQuery(query).executeUpdate()
    }

    override fun delete(entity: WorkingHours) {
        em.remove(if (em.contains(entity)) entity else em.merge(entity))
        em.flush()
    }
}