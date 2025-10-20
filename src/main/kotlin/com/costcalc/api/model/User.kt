package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

typealias UserId = UUID

data class User(
    @Id
    val id: UserId,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val roles: Set<String> = setOf("ROLE_USER"),
    val companyName: String? = null,
    val phone: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null,
    val isEnabled: Boolean = true,
    val isEmailVerified: Boolean = false,
    val isAccountNonExpired: Boolean = true,
    val isAccountNonLocked: Boolean = true,
    val isCredentialsNonExpired: Boolean = true,
)
