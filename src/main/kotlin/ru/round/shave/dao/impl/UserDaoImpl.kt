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

    override fun getOrCreate(tgUser: com.github.kotlintelegrambot.entities.User, chatId: Long): User {
        val existing = getById(tgUser.id)
        if (existing != null) {
            return existing
        }
        val newUser = fromFromTg(tgUser, chatId)
        insert(newUser)
        return newUser
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

    private fun fromFromTg(user: com.github.kotlintelegrambot.entities.User, chatId: Long): User {
        return User(
            id = user.id,
            chatId = chatId,
            isBot = user.isBot,
            firstName = user.firstName,
            lastName = user.lastName,
            username = user.username,
            phone = null,
            replaceableMessageId = null,
            languageCode = user.languageCode,
            canJoinGroups = false,//user.canJoinGroups,
            canReadAllGroupMessages = false,//user.canReadAllGroupMessages,
            supportsInlineQueries = false,//user.supportsInlineQueries,
        )
    }
}