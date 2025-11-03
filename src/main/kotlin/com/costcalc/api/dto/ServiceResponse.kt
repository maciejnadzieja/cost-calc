package com.costcalc.api.dto

import com.costcalc.api.model.WorkUnit
import java.math.BigDecimal
import java.util.UUID

data class ServiceResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val workUnit: WorkUnit,
)
