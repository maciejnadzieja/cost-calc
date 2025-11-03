package com.costcalc.api.service

import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.MaterialRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class MaterialService(
    private val materialRepository: MaterialRepository,
) {
    fun search(
        userId: UserId,
        query: String?,
        pageable: Pageable,
    ): Page<Material> =
        if (query.isNullOrBlank()) {
            materialRepository.findByUserId(userId, pageable)
        } else {
            materialRepository
                .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    userId,
                    query,
                    query,
                    pageable,
                )
        }

    fun findByIdAndUserId(
        id: UUID,
        userId: UserId,
    ): Material =
        materialRepository
            .findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Material not found")

    fun create(material: Material): Material = materialRepository.save(material)

    fun update(
        id: UUID,
        name: String,
        description: String,
        price: BigDecimal,
        unit: MaterialUnit,
        category: String,
        userId: UserId,
    ): Material {
        val existingMaterial =
            materialRepository
                .findByIdAndUserId(id, userId)
                ?: throw NoSuchElementException("Material not found")

        val updatedMaterial =
            existingMaterial.copy(
                name = name,
                description = description,
                price = price,
                unit = unit,
                category = category,
            )

        return materialRepository.save(updatedMaterial)
    }

    fun delete(
        id: UUID,
        userId: UserId,
    ) {
        materialRepository
            .findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Material not found")
        materialRepository.deleteByIdAndUserId(id, userId)
    }
}
