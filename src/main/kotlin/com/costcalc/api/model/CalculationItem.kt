package com.costcalc.api.model

import java.math.BigDecimal

data class CalculationItem(
    val name: String,
    val quantity: BigDecimal,
    val unit: String,
    val unitPrice: BigDecimal,
    val type: ItemType,
) {
    val amount: BigDecimal = quantity.multiply(unitPrice)
}

enum class ItemType {
    SERVICE,
    MATERIAL,
}
