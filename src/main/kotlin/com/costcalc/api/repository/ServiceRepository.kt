package com.costcalc.api.repository

import com.costcalc.api.model.Service
import com.costcalc.api.model.ServiceId
import com.costcalc.api.model.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : MongoRepository<Service, ServiceId> {
    fun findByUserId(userId: UserId): List<Service>

    fun findByUserId(userId: UserId, pageable: Pageable): Page<Service>

    fun findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        userId: UserId,
        name: String,
        description: String,
        pageable: Pageable,
    ): Page<Service>
}
