package com.costcalc.api.dto

import com.costcalc.api.model.CalculationStage
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CalculationRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    val name: String,
    
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String,
    
    @field:Valid
    val stages: List<CalculationStage>,
    
    @field:NotBlank(message = "Currency is required")
    @field:Pattern(regexp = "^(EUR|PLN|USD)$", message = "Currency must be EUR, PLN, or USD")
    val currency: String,
)
