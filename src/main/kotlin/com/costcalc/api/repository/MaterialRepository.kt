package com.costcalc.api.repository

import com.costcalc.api.dto.MaterialCategoryCount
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialId
import com.costcalc.api.model.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MaterialRepository : MongoRepository<Material, MaterialId> {
    fun deleteByUserIdAndCategory(
        userId: UserId,
        categoryId: String,
    )

    fun findByUserId(userId: UserId): List<Material>

    fun findByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Material>

    fun findByUserIdAndNameContainingIgnoreCase(
        userId: UserId,
        name: String,
        pageable: Pageable,
    ): Page<Material>

    fun findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        userId: UserId,
        name: String,
        description: String,
        pageable: Pageable,
    ): Page<Material>

    @Aggregation(
        pipeline = [
            "{ '\$match': { 'userId': ?0 } }",
            "{ '\$group': { _id: '\$category', count: { '\$sum': 1 } } }",
            "{ '\$project': { category: '\$_id', count: 1, _id: 0 } }",
        ],
    )
    fun countMaterialsByCategoryForUser(userId: UserId): List<MaterialCategoryCount>

    fun findByIdAndUserId(id: MaterialId, userId: UserId): java.util.Optional<Material>
}
