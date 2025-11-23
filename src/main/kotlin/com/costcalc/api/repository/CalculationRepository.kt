package com.costcalc.api.repository

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CalculationRepository : MongoRepository<Calculation, CalculationId> {
    fun findByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Calculation>

    @Query("{ 'userId': ?0, \$or: [ { 'name': { \$regex: ?1, \$options: 'i' } }, { 'description': { \$regex: ?1, \$options: 'i' } } ] }")
    fun findByUserIdAndQuery(
        userId: UserId,
        query: String,
        pageable: Pageable,
    ): Page<Calculation>

    fun findByIdAndUserId(
        id: CalculationId,
        userId: UserId,
    ): Calculation?

    fun deleteByIdAndUserId(
        id: CalculationId,
        userId: UserId,
    )
}
