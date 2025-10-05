package com.costcalc.api.controller

import com.costcalc.api.dto.CalculationForm
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.User
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.repository.CalculationRepository
import com.costcalc.api.repository.MaterialRepository
import com.costcalc.api.repository.ServiceRepository
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.UUID

@Controller
@RequestMapping("/calculations")
class CalculationController(
    private val calculationRepository: CalculationRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val serviceRepository: ServiceRepository,
    private val materialRepository: MaterialRepository,
) {
    @GetMapping
    fun listCalculations(
        model: Model,
        user: User,
    ): String {
        val calculations = calculationRepository.findByUserId(user.id)
        model.addAttribute("calculations", calculations)
        return "calculations/list"
    }

    @GetMapping("/new")
    fun newCalculation(
        model: Model,
        user: User,
    ): String {
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }
        model.addAttribute(
            "calculationForm",
            CalculationForm(
                currency = userSettings?.currency ?: "USD",
            ),
        )
        addUnitsToModel(model)
        return "calculations/form"
    }

    @PostMapping
    fun saveCalculation(
        @ModelAttribute calculationForm: CalculationForm,
        user: User,
        redirectAttributes: RedirectAttributes,
    ): String {
        val calculation = calculationForm.toCalculation(user.id)
        calculationRepository.save(calculation)
        redirectAttributes.addFlashAttribute("calculationSaved", true)
        return "redirect:/calculations/${calculation.id}/edit"
    }

    @GetMapping("/{id}/edit")
    fun editCalculation(
        @PathVariable id: CalculationId,
        model: Model,
        user: User,
    ): String {
        val calculation = getUserCalculation(id, user)
        model.addAttribute(
            "calculationForm",
            CalculationForm.fromCalculation(calculation),
        )
        addUnitsToModel(model)
        return "calculations/form"
    }

    @PostMapping("/{id}/delete")
    fun deleteCalculation(
        @PathVariable id: CalculationId,
        user: User,
    ): String {
        getUserCalculation(id, user)
        calculationRepository.deleteById(id)
        return "redirect:/calculations"
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateCalculation(
        @PathVariable id: CalculationId,
        user: User,
    ): String {
        val originalCalculation = getUserCalculation(id, user)
        val newCalculation = originalCalculation.copy(
            id = UUID.randomUUID(),
            name = "Copy of ${originalCalculation.name}"
        )
        calculationRepository.save(newCalculation)
        return "redirect:/calculations/${newCalculation.id}/edit"
    }

    @GetMapping("/{id}/print")
    fun printCalculation(
        @PathVariable id: CalculationId,
        model: Model,
        user: User,
    ): String {
        val calculation = getUserCalculation(id, user)
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }
        model.addAttribute("calculation", calculation)
        model.addAttribute("userSettings", userSettings)
        return "calculations/print"
    }

    @GetMapping("/templates/stage")
    fun getStageTemplate(
        model: Model,
        @RequestParam index: Int = 0,
    ): String {
        model.addAttribute("index", index)
        model.addAttribute("stage", index + 1)
        return "calculations/stage_template"
    }

    @GetMapping("/templates/item")
    fun getItemTemplate(
        model: Model,
        @RequestParam itemId: UUID,
        @RequestParam stageIndex: Int = 0,
        @RequestParam itemIndex: Int = 0,
        @RequestParam type: String = "services",
    ): String {
        val (itemName, itemPrice, itemUnit) =
            if (type == "services") {
                val item = serviceRepository.findById(itemId).orElseThrow { RuntimeException("Service not found!") }
                Triple(item.name, item.price, item.workUnit.name)
            } else {
                val item = materialRepository.findById(itemId).orElseThrow { RuntimeException("Material not found!") }
                Triple(item.name, item.price, item.unit.name)
            }

        model.addAttribute("itemName", itemName)
        model.addAttribute("itemPrice", itemPrice)
        model.addAttribute("itemUnit", itemUnit)
        model.addAttribute("stageIndex", stageIndex)
        model.addAttribute("itemIndex", itemIndex)
        model.addAttribute("type", itemIndex)
        model.addAttribute("fieldNamePrefix", "stages[$stageIndex].$type[$itemIndex].")
        model.addAttribute(
            "units",
            if (type == "services") {
                WorkUnit.entries.map { unit ->
                    getUnitOption("service", unit)
                }
            } else {
                MaterialUnit.entries.map { unit ->
                    getUnitOption("material", unit)
                }
            },
        )
        return "calculations/item_template"
    }

    private fun getUserCalculation(
        id: CalculationId,
        user: User,
    ) = calculationRepository
        .findById(id)
        .orElseThrow()
        .also { calculation ->
            if (calculation.userId != user.id) {
                throw IllegalStateException("Not authorized to access this calculation")
            }
        }

    private fun addUnitsToModel(model: Model) {
        val materialUnits =
            MaterialUnit.entries.map { unit ->
                getUnitOption("material", unit)
            }
        val workUnits =
            WorkUnit.entries.map { unit ->
                getUnitOption("service", unit)
            }

        model.addAttribute("materialUnits", materialUnits)
        model.addAttribute("workUnits", workUnits)
    }

    private fun getUnitOption(
        type: String,
        unit: Enum<*>,
    ): UnitOption {
        val basePath = "$type.unit.${unit.name.lowercase()}"
        return UnitOption(
            name = unit.name,
            label = "$basePath.label",
        )
    }
}

data class UnitOption(
    val name: String,
    val label: String,
)
