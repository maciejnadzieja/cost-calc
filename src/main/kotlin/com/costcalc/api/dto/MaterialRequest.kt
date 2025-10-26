package com.costcalc.api.dto

import com.costcalc.api.model.MaterialUnit
import java.math.BigDecimal

data class MaterialRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val unit: MaterialUnit,
    val category: String
)
