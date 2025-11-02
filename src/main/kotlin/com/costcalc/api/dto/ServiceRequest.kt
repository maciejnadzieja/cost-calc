package com.costcalc.api.dto

import com.costcalc.api.model.WorkUnit
import java.math.BigDecimal

data class ServiceRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val workUnit: WorkUnit,
)
