package ru.round.shave.dao.impl

import org.springframework.stereotype.Repository
import ru.round.shave.dao.UserDao
import ru.round.shave.entity.User
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class UserDaoImpl : UserDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun insert(entity: User) {
        em.persist(entity)
        em.flush()
    }

    override fun update(entity: User) {
        em.merge(entity)
        em.flush()
    }

    override fun delete(entity: User) {
        em.remove(if (em.contains(entity)) entity else em.merge(entity))
        em.flush()
    }

    override fun getById(id: Long): User? {
        return em.find(User::class.java, id)
    }

    override fun getAll(): List<User> {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(User::class.java)
        val root = query.from(User::class.java)

        query
            .select(root)
            .orderBy(cb.asc(root.get<Long>("id")))

        return em.createQuery(query).resultList
    }
}