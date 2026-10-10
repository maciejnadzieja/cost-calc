package com.costcalc.api.dto

data class AdminUser(val username: String, val email: String?, val enabled: Boolean)

data class AdminUserPage(
    val content: List<AdminUser>,
    val number: Int,
    val hasNext: Boolean,
    val size: Int = 20,
)
