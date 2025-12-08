package com.costcalc.api.model

import com.fasterxml.jackson.annotation.JsonProperty
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
    val currency: Currency,
    val stages: List<CalculationStage> = listOf(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val totalAmount: BigDecimal =
        stages.fold(BigDecimal.ZERO) { acc, stage -> acc + stage.totalAmount }

    fun update(
        name: String,
        description: String,
        stages: List<CalculationStage>,
        currency: Currency,
    ): Calculation =
        this.copy(
            name = name,
            description = description,
            stages = stages,
            currency = currency,
            updatedAt = Instant.now(),
        )

    fun duplicate(): Calculation =
        this.copy(
            id = UUID.randomUUID(),
            name = "Copy of $name",
        )

    companion object {
        fun create(
            name: String,
            description: String,
            userId: UserId,
            currency: Currency,
            stages: List<CalculationStage> = listOf(),
        ) = Calculation(
            id = UUID.randomUUID(),
            name = name,
            description = description,
            userId = userId,
            currency = currency,
            stages = stages,
        )
    }
}
