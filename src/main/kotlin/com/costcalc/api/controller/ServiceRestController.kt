package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.ServiceRequest
import com.costcalc.api.model.Service
import com.costcalc.api.model.User
import com.costcalc.api.repository.ServiceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/services")
@PreAuthorize("isAuthenticated()")
class ServiceRestController(
    private val serviceRepository: ServiceRepository,
) {
    @GetMapping
    fun getServices(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): Page<Service> {
        val pageable = PageRequest.of(page, size)
        return if (query.isNullOrBlank()) {
            serviceRepository.findByUserId(user.id, pageable)
        } else {
            serviceRepository
                .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    user.id,
                    query,
                    query,
                    pageable,
                )
        }
    }

    @GetMapping("/{id}")
    fun getServiceById(
        @PathVariable id: UUID,
        @CurrentUser user: User,
    ): Service =
        serviceRepository
            .findByIdAndUserId(id, user.id)
            .orElseThrow { NoSuchElementException("Service not found") }

    @PostMapping
    fun createService(
        @RequestBody serviceRequest: ServiceRequest,
        @CurrentUser user: User,
    ): Service {
        val newService =
            Service(
                id = UUID.randomUUID(),
                userId = user.id,
                name = serviceRequest.name,
                description = serviceRequest.description,
                price = serviceRequest.price,
                workUnit = serviceRequest.workUnit,
            )
        return serviceRepository.save(newService)
    }

    @PutMapping("/{id}")
    fun updateService(
        @PathVariable id: UUID,
        @RequestBody serviceRequest: ServiceRequest,
        @CurrentUser user: User,
    ): Service {
        val existingService =
            serviceRepository
                .findByIdAndUserId(id, user.id)
                .orElseThrow { NoSuchElementException("Service not found") }

        val updatedService =
            existingService.copy(
                name = serviceRequest.name,
                description = serviceRequest.description,
                price = serviceRequest.price,
                workUnit = serviceRequest.workUnit,
            )

        return serviceRepository.save(updatedService)
    }
}
