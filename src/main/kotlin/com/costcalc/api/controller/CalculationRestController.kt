package com.costcalc.api.controller

import com.costcalc.api.config.CurrencyConfig
import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.CalculationRequest
import com.costcalc.api.dto.CalculationResponse
import com.costcalc.api.mapper.toResponse
import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.Currency
import com.costcalc.api.model.User
import com.costcalc.api.service.CalculationService
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
import java.util.UUID

@RestController
@RequestMapping("/api/calculations")
@PreAuthorize("isAuthenticated()")
class CalculationRestController(
    private val calculationService: CalculationService,
    private val currencyConfig: CurrencyConfig,
) {
    @GetMapping
    fun getCalculations(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): Page<CalculationResponse> {
        val pageable = PageRequest.of(page, size)
        return calculationService.search(user.id, query, pageable).map { it.toResponse() }
    }

    @GetMapping("/currencies")
    fun getSupportedCurrencies(): List<String> = currencyConfig.supportedCurrencies

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ): CalculationResponse {
        val calculation = calculationService.findByIdAndUserId(id, user.id)
        return calculation.toResponse()
    }

    @PostMapping
    fun create(
        @Valid @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): CalculationResponse {
        val calculation =
            Calculation(
                id = UUID.randomUUID(),
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = Currency(calculationRequest.currency),
                userId = user.id,
            )
        val createdCalculation = calculationService.create(calculation)
        return createdCalculation.toResponse()
    }

    @PostMapping("/{id}/duplicate")
    fun duplicate(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ): CalculationResponse {
        val duplicatedCalculation = calculationService.duplicate(id, user.id)
        return duplicatedCalculation.toResponse()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: CalculationId,
        @Valid @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): CalculationResponse {
        val updatedCalculation =
            calculationService.update(
                id = id,
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = Currency(calculationRequest.currency),
                userId = user.id,
            )
        return updatedCalculation.toResponse()
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ) {
        calculationService.delete(id, user.id)
    }
}
