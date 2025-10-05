package com.costcalc.api.controller

import com.costcalc.api.dto.MaterialForm
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialId
import com.costcalc.api.model.User
import com.costcalc.api.repository.MaterialRepository
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader

@Controller
@RequestMapping("/materials")
class MaterialController(
    private val materialRepository: MaterialRepository,
    private val userSettingsRepository: UserSettingsRepository,
) {
    @GetMapping
    fun listMaterials(
        model: Model,
        user: User,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int,
        @RequestParam(name = "query", required = false) query: String?,
    ): String {
        val pageable: Pageable = PageRequest.of(page, size)
        val materialsPage =
            if (!query.isNullOrBlank()) {
                materialRepository.findByUserIdAndNameContainingIgnoreCase(user.id, query, pageable)
            } else {
                materialRepository.findByUserId(user.id, pageable)
            }
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }

        model.addAttribute("materialsPage", materialsPage)
        model.addAttribute("materials", materialsPage.content)
        model.addAttribute("currency", userSettings?.currency ?: "USD")
        model.addAttribute("categories", materialRepository.countMaterialsByCategoryForUser(user.id))
        model.addAttribute("currentPage", page)
        model.addAttribute("pageSize", size)
        model.addAttribute("query", query ?: "")
        return "materials/list"
    }

    @GetMapping("/new")
    fun newMaterial(model: Model): String {
        model.addAttribute("material", MaterialForm())
        return "materials/form"
    }

    @PostMapping
    fun saveMaterial(
        @ModelAttribute materialForm: MaterialForm,
        user: User,
    ): String {
        val material = materialForm.toMaterial(user.id)
        materialRepository.save(material)
        return "redirect:/materials/${material.id}/edit"
    }

    @GetMapping("/{id}/edit")
    fun editMaterial(
        @PathVariable id: MaterialId,
        model: Model,
        user: User,
    ): String {
        val material = getMaterialForUser(id, user)
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }

        model.addAttribute("material", MaterialForm.fromMaterial(material))
        model.addAttribute("currency", userSettings?.currency ?: "USD")
        return "materials/form"
    }

    @PostMapping("/{id}/delete")
    fun deleteMaterial(
        @PathVariable id: MaterialId,
        user: User,
    ): String {
        getMaterialForUser(id, user)
        materialRepository.deleteById(id)
        return "redirect:/materials"
    }

    @PostMapping("/category/{category}/delete")
    fun deleteMaterialsByCategory(
        @PathVariable category: String,
        user: User,
    ): String {
        materialRepository.deleteByUserIdAndCategory(user.id, category)
        return "redirect:/materials"
    }

    @GetMapping("/import")
    fun importMaterialsForm(): String = "materials/import"

    @PostMapping("/import")
    fun importMaterials(
        @RequestParam category: String,
        @RequestParam csv: String,
        user: User,
        model: Model,
    ): String {
        val lines = csv.lines().filter { it.isNotBlank() }
        val materials =
            lines.mapNotNull { line ->
                val parts = line.split(';')
                if (parts.size < 3) return@mapNotNull null
                val name = parts[0].trim()
                val priceStr = parts[1].trim().replace(",", ".")
                val price = priceStr.split(" ").firstOrNull()?.toBigDecimalOrNull() ?: return@mapNotNull null
                val description =
                    parts
                        .subList(2, parts.size)
                        .joinToString(";")
                        .trim()
                        .replace("%0A", "\n")
                MaterialForm(
                    name = name,
                    price = price,
                    description = description,
                    category = category,
                )
            }
        materials.forEach { materialForm ->
            val material = materialForm.toMaterial(user.id)
            materialRepository.save(material)
        }
        return "redirect:/materials"
    }

    @PostMapping("/import/csv", consumes = ["multipart/form-data"])
    fun importMaterialsCsv(
        @RequestPart("file") file: MultipartFile,
        @RequestParam category: String?,
        user: User,
    ): String {
        val reader = BufferedReader(InputStreamReader(file.inputStream))
        val lines = reader.lineSequence().filter { it.isNotBlank() }
        val materials =
            lines.mapNotNull { line ->
                val parts = line.split(';')
                if (parts.size < 3) return@mapNotNull null
                val name = parts[0].trim()
                val priceStr = parts[1].trim().replace(",", ".")
                val price = priceStr.split(" ").firstOrNull()?.toBigDecimalOrNull() ?: return@mapNotNull null
                val description =
                    parts
                        .subList(2, parts.size)
                        .joinToString(";")
                        .trim()
                        .replace("%0A", "\n")
                com.costcalc.api.dto.MaterialForm(
                    name = name,
                    price = price,
                    description = description,
                    category = category ?: "",
                )
            }
        materials.forEach { materialForm ->
            val material = materialForm.toMaterial(user.id)
            materialRepository.save(material)
        }
        return "redirect:/materials"
    }

    internal fun getMaterialForUser(
        id: MaterialId,
        user: User,
    ): Material {
        val material = materialRepository.findById(id).orElseThrow()
        if (material.userId != user.id) {
            throw IllegalStateException("Not authorized to access this material")
        }
        return material
    }
}
