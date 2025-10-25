package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.Service
import com.costcalc.api.model.User
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.repository.MaterialRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
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
    @GetMapping("/search")
    fun searchMaterials(
        @RequestParam query: String,
        @CurrentUser user: User,
    ): List<Material> {

        return materialRepository
            .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                user.id,
                query,
                query,
                PageRequest.of(0, 10),
            ).content
    }
}