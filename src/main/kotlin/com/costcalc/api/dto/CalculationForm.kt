package com.costcalc.api.dto

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationItem
import com.costcalc.api.model.CalculationStage
import com.costcalc.api.model.ItemType
import com.costcalc.api.model.UserId
import java.math.BigDecimal
import java.util.UUID

data class CalculationForm(
    var id: UUID? = null,
    var name: String = "",
    var description: String = "",
    var currency: String = "",
    var stages: MutableList<CalculationStageForm> = mutableListOf(),
) {
    fun toCalculation(userId: UserId) =
        Calculation(
            id = id ?: UUID.randomUUID(),
            name = name,
            description = description,
            userId = userId,
            currency = currency,
            stages = stages.map { it.toCalculationStage() }.toMutableList(),
        )

    companion object {
        fun fromCalculation(calculation: Calculation): CalculationForm =
            CalculationForm(
                id = calculation.id,
                name = calculation.name,
                description = calculation.description,
                currency = calculation.currency,
                stages =
                    calculation.stages
                        .map { stage ->
                            CalculationStageForm(
                                name = stage.name,
                                services =
                                    stage.services
                                        .map { service ->
                                            CalculationItemForm(
                                                name = service.name,
                                                quantity = service.quantity,
                                                unit = service.unit,
                                                unitPrice = service.unitPrice,
                                            )
                                        }.toMutableList(),
                                materials =
                                    stage.materials
                                        .map { material ->
                                            CalculationItemForm(
                                                name = material.name,
                                                quantity = material.quantity,
                                                unit = material.unit,
                                                unitPrice = material.unitPrice,
                                            )
                                        }.toMutableList(),
                            )
                        }.toMutableList(),
            )
    }
}

data class CalculationStageForm(
    var name: String = "",
    var services: MutableList<CalculationItemForm> = mutableListOf(),
    var materials: MutableList<CalculationItemForm> = mutableListOf(),
) {
    val materialsSummary: Map<String, BigDecimal>
        get() = materials.groupBy { it.unit }
            .mapValues { (_, items) -> items.sumOf { it.quantity } }

    fun toCalculationStage() =
        CalculationStage(
            name = name,
            services = services.map { it.toCalculationItem(ItemType.SERVICE) }.toMutableList(),
            materials = materials.map { it.toCalculationItem(ItemType.MATERIAL) }.toMutableList(),
        )
}

data class CalculationItemForm(
    var name: String = "",
    var quantity: BigDecimal = BigDecimal.ZERO,
    var unit: String = "",
    var unitPrice: BigDecimal = BigDecimal.ZERO,
) {
    fun toCalculationItem(type: ItemType) =
        CalculationItem(
            name = name,
            quantity = quantity,
            unit = unit,
            unitPrice = unitPrice,
            type = type,
        )
}
