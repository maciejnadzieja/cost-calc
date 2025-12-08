package com.costcalc.api.dto

import com.costcalc.api.model.Currency
import com.costcalc.api.validation.SupportedCurrency
import jakarta.validation.constraints.Size

data class UserSettingsRequest(
    @field:SupportedCurrency
    val currency: Currency,
    @field:Size(max = 200, message = "Footer text must not exceed 200 characters")
    val footerText: String,
)
