package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.ServiceRequest
import com.costcalc.api.dto.ServiceResponse
import com.costcalc.api.mapper.toResponse
import com.costcalc.api.model.Service
import com.costcalc.api.model.User
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.service.ServiceService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
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
    private val serviceService: ServiceService,
) {
    @GetMapping
    fun getServices(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): Page<ServiceResponse> {
        val pageable = PageRequest.of(page, size)
        return serviceService.search(user.id, query, pageable).map { it.toResponse() }
    }

    @GetMapping("/units")
    fun getWorkUnits(): List<WorkUnit> = WorkUnit.entries

    @GetMapping("/{id}")
    fun getServiceById(
        @PathVariable id: UUID,
        @CurrentUser user: User,
    ): ServiceResponse = serviceService.findByIdAndUserId(id, user.id).toResponse()

    @PostMapping
    fun createService(
        @RequestBody serviceRequest: ServiceRequest,
        @CurrentUser user: User,
    ): ServiceResponse {
        val newService =
            Service.create(
                userId = user.id,
                name = serviceRequest.name,
                description = serviceRequest.description,
                price = serviceRequest.price,
                workUnit = serviceRequest.workUnit,
            )
        return serviceService.create(newService).toResponse()
    }

    @PutMapping("/{id}")
    fun updateService(
        @PathVariable id: UUID,
        @RequestBody serviceRequest: ServiceRequest,
        @CurrentUser user: User,
    ): ServiceResponse =
        serviceService
            .update(
                id = id,
                name = serviceRequest.name,
                description = serviceRequest.description,
                price = serviceRequest.price,
                workUnit = serviceRequest.workUnit,
                userId = user.id,
            ).toResponse()

    @DeleteMapping("/{id}")
    fun deleteService(
        @PathVariable id: UUID,
        @CurrentUser user: User,
    ) {
        serviceService.delete(id, user.id)
    }
}
