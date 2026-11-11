package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.MaterialRequest
import com.costcalc.api.dto.MaterialResponse
import com.costcalc.api.mapper.toResponse
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.User
import com.costcalc.api.search.MaterialSourceLinks
import com.costcalc.api.service.MaterialService
import jakarta.validation.Valid
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
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/materials")
@PreAuthorize("isAuthenticated()")
class MaterialRestController(
    private val materialService: MaterialService,
    private val sourceLinks: MaterialSourceLinks,
) {
    @GetMapping
    fun getMaterials(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): Page<MaterialResponse> {
        val pageable = PageRequest.of(page, size)
        return materialService.search(user.id, query, pageable).map { it.toResponse() }
    }

    @GetMapping("/units")
    fun getMaterialUnits(): List<MaterialUnit> = MaterialUnit.entries

    @GetMapping("/{id}")
    fun getMaterialById(
        @PathVariable id: UUID,
        @CurrentUser user: User,
    ): MaterialResponse = materialService.findByIdAndUserId(id, user.id).toResponse()

    @PostMapping
    fun createMaterial(
        @Valid @RequestBody materialRequest: MaterialRequest,
        @CurrentUser user: User,
    ): MaterialResponse {
        val sourceUrl = materialRequest.sourceUrl
        val imageUrl = materialRequest.imageUrl
        require(sourceUrl != null || imageUrl == null) { "A material image requires a source URL" }
        if (sourceUrl != null) {
            sourceLinks.validate(sourceUrl, imageUrl)
            validateSourcedMaterial(materialRequest)
        }
        return materialService.create(
            Material.create(
                name = materialRequest.name,
                description = materialRequest.description,
                price = materialRequest.price,
                unit = materialRequest.unit,
                category = materialRequest.category,
                userId = user.id,
                sourceUrl = sourceUrl,
                imageUrl = imageUrl,
            ),
        ).toResponse()
    }

    private fun validateSourcedMaterial(materialRequest: MaterialRequest) {
        require(materialRequest.name.isNotBlank() && materialRequest.name.length <= 100) {
            "Name must be between 1 and 100 characters"
        }
        require(materialRequest.description.length <= 500) { "Description must not exceed 500 characters" }
        require(materialRequest.price >= BigDecimal("0.01")) { "Price must be at least 0.01" }
        require(materialRequest.category.isNotBlank() && materialRequest.category.length <= 50) {
            "Category must be between 1 and 50 characters"
        }
    }

    @PutMapping("/{id}")
    fun updateMaterial(
        @PathVariable id: UUID,
        @Valid @RequestBody materialRequest: MaterialRequest,
        @CurrentUser user: User,
    ): MaterialResponse =
        materialService
            .update(
                id = id,
                name = materialRequest.name,
                description = materialRequest.description,
                price = materialRequest.price,
                unit = materialRequest.unit,
                category = materialRequest.category,
                userId = user.id,
            ).toResponse()

    @DeleteMapping("/{id}")
    fun deleteMaterial(
        @PathVariable id: UUID,
        @CurrentUser user: User,
    ) {
        materialService.delete(id, user.id)
    }
}
