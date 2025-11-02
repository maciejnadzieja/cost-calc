package com.costcalc.api.model

import java.math.BigDecimal

data class CalculationStage(
    val name: String,
    val services: List<CalculationItem> = listOf(),
    val materials: List<CalculationItem> = listOf(),
) {
    fun getServicesTotal(): BigDecimal = services.sumOf { it.getTotalAmount() }

    fun getMaterialsTotal(): BigDecimal = materials.sumOf { it.getTotalAmount() }

    fun getTotalAmount(): BigDecimal = getServicesTotal() + getMaterialsTotal()
}
