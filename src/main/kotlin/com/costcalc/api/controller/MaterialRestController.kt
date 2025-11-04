package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.MaterialRequest
import com.costcalc.api.dto.MaterialResponse
import com.costcalc.api.mapper.toResponse
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.User
import com.costcalc.api.service.MaterialService
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
@RequestMapping("/api/materials")
@PreAuthorize("isAuthenticated()")
class MaterialRestController(
    private val materialService: MaterialService,
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
        @RequestBody materialRequest: MaterialRequest,
        @CurrentUser user: User,
    ): MaterialResponse {
        val newMaterial =
            Material.create(
                name = materialRequest.name,
                description = materialRequest.description,
                price = materialRequest.price,
                unit = materialRequest.unit,
                category = materialRequest.category,
                userId = user.id,
            )
        return materialService.create(newMaterial).toResponse()
    }

    @PutMapping("/{id}")
    fun updateMaterial(
        @PathVariable id: UUID,
        @RequestBody materialRequest: MaterialRequest,
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
