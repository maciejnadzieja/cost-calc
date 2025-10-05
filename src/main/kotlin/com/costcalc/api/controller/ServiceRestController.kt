package com.costcalc.api.controller

import com.costcalc.api.model.Service
import com.costcalc.api.model.User
import com.costcalc.api.repository.ServiceRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/services")
class ServiceRestController(
    private val serviceRepository: ServiceRepository,
) {
    @GetMapping("/search")
    fun searchServices(
        @RequestParam query: String,
        user: User,
    ): List<Service> {
        val services = serviceRepository.findByUserId(user.id)
        return services.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    }
}
