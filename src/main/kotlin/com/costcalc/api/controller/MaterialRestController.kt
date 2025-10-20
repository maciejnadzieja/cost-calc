package com.costcalc.api.controller

import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.repository.MaterialRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/materials")
@PreAuthorize("isAuthenticated()")
class MaterialRestController(
    private val materialRepository: MaterialRepository,
) {
    @GetMapping("/search")
    fun searchMaterials(
        @RequestParam query: String,
        auth: Authentication,
    ): List<Material> {
        val email = (auth.principal as Jwt).getClaimAsString("email")
        val userId = UUID.nameUUIDFromBytes(email.toByteArray())
        println(userId)
        val materials = materialRepository.findByUserId(userId)
        return materials.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    }
}
