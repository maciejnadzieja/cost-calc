package com.costcalc.api.dto

data class UserSettingsForm(
    val currency: String = "USD",
    val themeColor: String = "#007bff",
    val footerText: String = "",
)
