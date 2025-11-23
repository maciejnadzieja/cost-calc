package com.costcalc.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserSettingsRequest(
    @field:NotBlank(message = "Currency is required")
    @field:Pattern(regexp = "^(EUR|PLN|USD)$", message = "Currency must be EUR, PLN, or USD")
    val currency: String,
    @field:NotBlank(message = "Theme color is required")
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Theme color must be a valid hex color")
    val themeColor: String,
    @field:Size(max = 200, message = "Footer text must not exceed 200 characters")
    val footerText: String,
)
