package com.costcalc.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class CalculationItem(
    val name: String,
    val quantity: BigDecimal,
    val unit: String,
    val unitPrice: BigDecimal,
    val type: ItemType,
) {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val amount: BigDecimal = quantity.multiply(unitPrice)

    init {
        require(quantity > BigDecimal.ZERO)
        require(unitPrice > BigDecimal.ZERO)
    }
}

enum class ItemType {
    SERVICE,
    MATERIAL,
}
