package com.costcalc.api.dto

import com.costcalc.api.model.ItemType
import java.math.BigDecimal

data class CalculationItemResponse(
    val name: String,
    val quantity: BigDecimal,
    val unit: String,
    val unitPrice: BigDecimal,
    val type: ItemType,
    val amount: BigDecimal,
)
