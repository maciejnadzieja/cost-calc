package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "user_settings")
data class UserSettings(
    @Id
    val id: UserId,
    val currency: String = "USD", // Default currency
    val themeColor: String = "#007bff", // Default bootstrap primary color
    val footerText: String = "", // Default empty footer
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
