package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.model.Material
import com.costcalc.api.model.User
import com.costcalc.api.repository.MaterialRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/materials")
@PreAuthorize("isAuthenticated()")
class MaterialRestController(
    private val materialRepository: MaterialRepository,
) {
    @GetMapping
    fun getMaterials(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): Page<Material> {
        val pageable = PageRequest.of(page, size)
        return if (query.isNullOrBlank()) {
            materialRepository.findByUserId(user.id, pageable)
        } else {
            materialRepository
                .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    user.id,
                    query,
                    query,
                    pageable,
                )
        }
    }
}