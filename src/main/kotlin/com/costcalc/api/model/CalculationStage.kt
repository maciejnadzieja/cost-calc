package com.costcalc.api.model

import java.math.BigDecimal

data class CalculationStage(
    val name: String,
    val services: List<CalculationItem> = listOf(),
    val materials: List<CalculationItem> = listOf(),
) {
    val servicesTotalAmount: BigDecimal = services.sumOf { it.amount }
    val materialsTotalAmount: BigDecimal = materials.sumOf { it.amount }

    val totalAmount: BigDecimal = servicesTotalAmount + materialsTotalAmount

    init {
        require(servicesTotalAmount > BigDecimal.ZERO)
        require(materialsTotalAmount > BigDecimal.ZERO)
    }
}
