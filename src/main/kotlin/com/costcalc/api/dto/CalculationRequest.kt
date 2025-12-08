package com.costcalc.api.dto

import com.costcalc.api.model.CalculationStage
import com.costcalc.api.model.Currency
import com.costcalc.api.validation.SupportedCurrency
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CalculationRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    val name: String,
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String,
    @field:Valid
    val stages: List<CalculationStage>,
    @field:SupportedCurrency
    val currency: Currency,
)
