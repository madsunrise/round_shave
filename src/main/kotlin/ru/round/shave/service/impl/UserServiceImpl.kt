package ru.round.shave.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.round.shave.dao.UserDao
import ru.round.shave.entity.User
import ru.round.shave.service.UserService

@Service
@Transactional(rollbackFor = [Exception::class])
open class UserServiceImpl : UserService {

    @Autowired
    private lateinit var userDao: UserDao

    override fun insert(entity: User) {
        userDao.insert(entity)
    }

    override fun update(entity: User) {
        userDao.update(entity)
    }

    override fun delete(entity: User) {
        userDao.delete(entity)
    }

    override fun getById(id: Long): User? {
        return userDao.getById(id)
    }

    override fun getAll(): List<User> {
        return userDao.getAll()
    }
}