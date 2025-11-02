package com.costcalc.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

typealias CalculationId = UUID

@Document(collection = "calculations")
data class Calculation(
    @Id
    val id: CalculationId,
    val name: String,
    val description: String,
    val userId: UserId,
    val currency: String,
    val stages: List<CalculationStage> = listOf(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    val totalAmount: BigDecimal =
        stages.fold(BigDecimal.ZERO) { acc, stage -> acc + stage.totalAmount }
}
