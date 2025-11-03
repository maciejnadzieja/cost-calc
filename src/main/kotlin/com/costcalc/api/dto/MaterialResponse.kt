package com.costcalc.api.dto

import com.costcalc.api.model.MaterialUnit
import java.math.BigDecimal
import java.util.UUID

data class MaterialResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val unit: MaterialUnit,
    val category: String?,
)
