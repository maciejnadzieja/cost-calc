package com.costcalc.api.dto

import com.costcalc.api.validation.SupportedCurrency
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserSettingsRequest(
    @field:NotBlank(message = "Currency is required")
    @field:SupportedCurrency
    val currency: String,
    @field:Size(max = 200, message = "Footer text must not exceed 200 characters")
    val footerText: String,
)
