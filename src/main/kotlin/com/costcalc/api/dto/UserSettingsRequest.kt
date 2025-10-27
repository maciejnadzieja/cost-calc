package com.costcalc.api.dto

data class UserSettingsRequest(
    val currency: String,
    val themeColor: String,
    val footerText: String,
)
