package com.costcalc.api.dto

import com.costcalc.api.model.CalculationStage

data class CalculationRequest(
    val name: String,
    val description: String,
    val stages: List<CalculationStage>,
    val currency: String
)
