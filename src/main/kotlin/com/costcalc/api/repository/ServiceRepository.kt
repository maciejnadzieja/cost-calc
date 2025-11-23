package com.costcalc.api.repository

import com.costcalc.api.model.Service
import com.costcalc.api.model.ServiceId
import com.costcalc.api.model.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : MongoRepository<Service, ServiceId> {
    fun findByUserId(userId: UserId): List<Service>

    fun findByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Service>

    @Query("{ 'userId': ?0, \$or: [ { 'name': { \$regex: ?1, \$options: 'i' } }, { 'description': { \$regex: ?1, \$options: 'i' } } ] }")
    fun findByUserIdAndQuery(
        userId: UserId,
        query: String,
        pageable: Pageable,
    ): Page<Service>

    fun findByIdAndUserId(
        id: ServiceId,
        userId: UserId,
    ): Service?

    fun deleteByIdAndUserId(
        id: ServiceId,
        userId: UserId,
    )
}
