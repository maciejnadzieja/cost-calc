package com.costcalc.api.service

import com.costcalc.api.model.Service
import com.costcalc.api.model.UserId
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.repository.ServiceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Service as SpringService

@SpringService
class ServiceService(
    private val serviceRepository: ServiceRepository,
) {
    fun search(
        userId: UserId,
        query: String?,
        pageable: Pageable,
    ): Page<Service> =
        if (query.isNullOrBlank()) {
            serviceRepository.findByUserId(userId, pageable)
        } else {
            serviceRepository.findByUserIdAndQuery(userId, Regex.escape(query), pageable)
        }

    fun findByIdAndUserId(
        id: UUID,
        userId: UserId,
    ): Service =
        serviceRepository
            .findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Service not found")

    fun create(service: Service): Service = serviceRepository.save(service)

    fun update(
        id: UUID,
        name: String,
        description: String,
        price: BigDecimal,
        workUnit: WorkUnit,
        userId: UserId,
    ): Service {
        val existingService =
            serviceRepository
                .findByIdAndUserId(id, userId)
                ?: throw NoSuchElementException("Service not found")

        return serviceRepository.save(
            existingService.update(
                name = name,
                description = description,
                price = price,
                workUnit = workUnit,
            ),
        )
    }

    fun delete(
        id: UUID,
        userId: UserId,
    ) {
        serviceRepository.findByIdAndUserId(id, userId) ?: throw NoSuchElementException("Service not found")
        serviceRepository.deleteByIdAndUserId(id, userId)
    }
}
