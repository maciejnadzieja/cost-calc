package com.costcalc.api.repository

import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialId
import com.costcalc.api.model.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MaterialRepository : MongoRepository<Material, MaterialId> {
    fun findByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Material>

    @Query("{ 'userId': ?0, \$or: [ { 'searchName': { \$regex: ?1 } }, { 'searchDescription': { \$regex: ?1 } } ] }")
    fun findByUserIdAndQuery(
        userId: UserId,
        query: String,
        pageable: Pageable,
    ): Page<Material>

    fun findByIdAndUserId(
        id: MaterialId,
        userId: UserId,
    ): Material?

    fun deleteByIdAndUserId(
        id: MaterialId,
        userId: UserId,
    )
}
