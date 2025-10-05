package com.costcalc.api.repository

import com.costcalc.api.model.User
import com.costcalc.api.model.UserId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, UserId> {
    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean
}
