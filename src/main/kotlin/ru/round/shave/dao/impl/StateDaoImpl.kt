package ru.round.shave.dao.impl

import org.springframework.stereotype.Repository
import ru.round.shave.dao.StateDao
import ru.round.shave.entity.State
import ru.round.shave.entity.User
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Predicate

@Repository
class StateDaoImpl : StateDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun insert(entity: State) {
        em.persist(entity)
        em.flush()
    }

    override fun update(entity: State) {
        em.merge(entity)
        em.flush()
    }

    override fun delete(entity: State) {
        em.remove(if (em.contains(entity)) entity else em.merge(entity))
        em.flush()
    }

    override fun getById(id: Long): State? {
        return em.find(State::class.java, id)
    }

    override fun getUserState(user: User): State? {
        val cb = em.criteriaBuilder
        val query = cb.createQuery(State::class.java)
        val root = query.from(State::class.java)

        val predicates = mutableListOf<Predicate>()
        predicates.add(cb.equal(root.get<User>("user"), user))

        query
            .select(root)
            .where(*predicates.toTypedArray())

        return em.createQuery(query).resultList.firstOrNull()
    }

    override fun clearState(user: User) {
        val cb = em.criteriaBuilder
        val query = cb.createCriteriaDelete(State::class.java)
        val root = query.from(State::class.java)
        query.where(cb.equal(root.get<User>("user"), user))
        em.createQuery(query).executeUpdate()
    }
}