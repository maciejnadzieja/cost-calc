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
) {
    fun update(
        name: String,
        description: String,
        price: BigDecimal,
        workUnit: WorkUnit,
    ): Service {
        require(price > BigDecimal.ZERO)
        require(name.isNotBlank())

        return this.copy(
            name = name,
            description = description,
            price = price,
            workUnit = workUnit,
        )
    }

    companion object {
        fun create(
            name: String,
            description: String,
            price: BigDecimal,
            workUnit: WorkUnit,
            userId: UserId,
        ): Service {
            require(price > BigDecimal.ZERO)
            require(name.isNotBlank())
            return Service(
                id = UUID.randomUUID(),
                name = name,
                description = description,
                price = price,
                workUnit = workUnit,
                userId = userId,
            )
        }
    }
}
