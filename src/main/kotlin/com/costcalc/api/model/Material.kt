package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

typealias MaterialId = UUID

@Document(collection = "materials")
data class Material(
    @Id
    val id: MaterialId,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val unit: MaterialUnit,
    val category: String,
    val userId: UserId,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
