package com.costcalc.api.service

import com.costcalc.api.model.Service
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.ServiceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
            serviceRepository
                .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    userId,
                    query,
                    query,
                    pageable,
                )
        }

    fun findByIdAndUserId(
        id: UUID,
        userId: UserId,
    ): Service =
        serviceRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow { NoSuchElementException("Service not found") }

    fun create(service: Service): Service = serviceRepository.save(service)

    fun update(
        id: UUID,
        service: Service,
    ): Service {
        val existingService =
            serviceRepository
                .findByIdAndUserId(id, service.userId)
                .orElseThrow { NoSuchElementException("Service not found") }

        val updatedService =
            existingService.copy(
                name = service.name,
                description = service.description,
                price = service.price,
                workUnit = service.workUnit,
            )

        return serviceRepository.save(updatedService)
    }

    fun delete(
        id: UUID,
        userId: UserId,
    ) {
        serviceRepository.deleteByIdAndUserId(id, userId)
    }
}
