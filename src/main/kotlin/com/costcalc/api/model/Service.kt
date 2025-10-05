package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

typealias ServiceId = UUID

@Document(collection = "services")
data class Service(
    @Id
    val id: ServiceId,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val workUnit: WorkUnit,
    val userId: UserId,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
