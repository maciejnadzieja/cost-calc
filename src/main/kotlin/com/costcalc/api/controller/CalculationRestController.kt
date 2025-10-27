package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.CalculationRequest
import com.costcalc.api.model.Calculation
import com.costcalc.api.model.User
import com.costcalc.api.service.CalculationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/calculations")
class CalculationRestController(
    private val calculationService: CalculationService,
) {
    @GetMapping
    fun getAll(
        @CurrentUser user: User,
    ): ResponseEntity<List<Calculation>> {
        val calculations = calculationService.getAllByUserId(user.id)
        return ResponseEntity.ok(calculations)
    }

    @PostMapping
    fun create(
        @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): ResponseEntity<Calculation> {
        val calculation =
            Calculation(
                id = UUID.randomUUID(),
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = calculationRequest.currency,
                userId = user.id,
            )
        val createdCalculation = calculationService.create(calculation)
        return ResponseEntity.ok(createdCalculation)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): ResponseEntity<Calculation> {
        val calculation =
            Calculation(
                id = id,
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = calculationRequest.currency,
                userId = user.id,
            )
        val updatedCalculation = calculationService.update(id.toString(), calculation)
        return ResponseEntity.ok(updatedCalculation)
    }
}
