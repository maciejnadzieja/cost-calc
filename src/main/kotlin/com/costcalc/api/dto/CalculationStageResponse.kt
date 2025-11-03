package com.costcalc.api.dto

import java.math.BigDecimal

data class CalculationStageResponse(
    val name: String,
    val services: List<CalculationItemResponse> = listOf(),
    val materials: List<CalculationItemResponse> = listOf(),
    val servicesTotalAmount: BigDecimal,
    val materialsTotalAmount: BigDecimal,
    val totalAmount: BigDecimal,
)
