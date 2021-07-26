package ru.round.shave.dao.impl

import org.springframework.stereotype.Repository
import ru.round.shave.dao.ServiceDao
import ru.round.shave.entity.Service
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class ServiceDaoImpl : ServiceDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun getById(id: Long): Service? {
        return em.find(Service::class.java, id)
    }

    override fun getAll(): List<Service> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(Service::class.java)
        val root = query.from(Service::class.java)

        query
            .select(root)
            .orderBy(cb.asc(root.get<Long>("id")))

        return em.createQuery(query).resultList
    }
}