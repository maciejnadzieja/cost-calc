package com.costcalc.api.repository

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.UserId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CalculationRepository : MongoRepository<Calculation, CalculationId> {
    fun findByUserId(userId: UserId): List<Calculation>
}
