package com.costcalc.api.repository

import com.costcalc.api.model.Service
import com.costcalc.api.model.ServiceId
import com.costcalc.api.model.UserId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : MongoRepository<Service, ServiceId> {
    fun findByUserId(userId: UserId): List<Service>
}
