package com.costcalc.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class CalculationStage(
    val name: String,
    val services: List<CalculationItem> = listOf(),
    val materials: List<CalculationItem> = listOf(),
) {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val servicesTotalAmount: BigDecimal = services.sumOf { it.amount }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val materialsTotalAmount: BigDecimal = materials.sumOf { it.amount }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val totalAmount: BigDecimal = servicesTotalAmount + materialsTotalAmount

    init {
        require(servicesTotalAmount >= BigDecimal.ZERO)
        require(materialsTotalAmount >= BigDecimal.ZERO)
    }
}
