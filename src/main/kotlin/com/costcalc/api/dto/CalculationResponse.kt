package com.costcalc.api.dto

import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.Currency
import java.math.BigDecimal
import java.time.Instant

data class CalculationResponse(
    val id: CalculationId,
    val name: String,
    val description: String,
    val currency: Currency,
    val stages: List<CalculationStageResponse> = listOf(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val totalAmount: BigDecimal,
)
