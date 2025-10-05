package com.costcalc.api.controller

import com.costcalc.api.model.Material
import com.costcalc.api.model.User
import com.costcalc.api.repository.MaterialRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/materials")
class MaterialRestController(
    private val materialRepository: MaterialRepository,
) {
    @GetMapping("/search")
    fun searchMaterials(
        @RequestParam query: String,
        user: User,
    ): List<Material> {
        val materials = materialRepository.findByUserId(user.id)
        return materials.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    }
}
