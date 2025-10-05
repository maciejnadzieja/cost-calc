package com.costcalc.api.dto

import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialId
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.UserId
import java.math.BigDecimal
import java.util.UUID

data class MaterialForm(
    val id: MaterialId? = null,
    val name: String = "",
    val description: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    val unit: MaterialUnit = MaterialUnit.PIECE,
    val category: String = "",
) {
    fun toMaterial(userId: UserId) =
        Material(
            id = id ?: UUID.randomUUID(),
            name = name,
            description = description,
            price = price,
            unit = unit,
            category = category,
            userId = userId,
        )

    companion object {
        fun fromMaterial(material: Material) =
            MaterialForm(
                id = material.id,
                name = material.name,
                description = material.description,
                price = material.price,
                unit = material.unit,
                category = material.category,
            )
    }
}
