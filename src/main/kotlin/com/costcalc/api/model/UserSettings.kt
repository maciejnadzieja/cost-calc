package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "user_settings")
data class UserSettings(
    @Id
    val id: UserId,
    val currency: Currency = Currency("USD"), // Default currency
    val footerText: String = "", // Default empty footer
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
