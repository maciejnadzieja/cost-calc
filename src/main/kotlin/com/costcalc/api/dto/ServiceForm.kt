package com.costcalc.api.dto

import com.costcalc.api.model.Service
import com.costcalc.api.model.ServiceId
import com.costcalc.api.model.UserId
import com.costcalc.api.model.WorkUnit
import java.math.BigDecimal
import java.util.UUID

data class ServiceForm(
    val id: ServiceId? = null,
    val name: String = "",
    val description: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    val workUnit: WorkUnit = WorkUnit.HOUR,
) {
    fun toService(userId: UserId) =
        Service(
            id = id ?: UUID.randomUUID(),
            name = name,
            description = description,
            price = price,
            workUnit = workUnit,
            userId = userId,
        )

    companion object {
        fun fromService(service: Service) =
            ServiceForm(
                id = service.id,
                name = service.name,
                description = service.description,
                price = service.price,
                workUnit = service.workUnit,
            )
    }
}
