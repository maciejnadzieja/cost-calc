package com.costcalc.api.dto

import com.costcalc.api.model.MaterialUnit
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class MaterialRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    val name: String,
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String,
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal,
    val unit: MaterialUnit,
    @field:NotBlank(message = "Category is required")
    @field:Size(max = 50, message = "Category must not exceed 50 characters")
    val category: String,
)
