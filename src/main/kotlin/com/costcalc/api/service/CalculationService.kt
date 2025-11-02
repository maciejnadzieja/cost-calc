package com.costcalc.api.service

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.CalculationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CalculationService(
    private val calculationRepository: CalculationRepository,
) {
    fun create(calculation: Calculation): Calculation = calculationRepository.save(calculation)

    fun findById(id: CalculationId): Calculation =
        calculationRepository.findById(id).orElse(null)
            ?: throw Exception("Calculation not found")

    fun update(
        id: CalculationId,
        calculation: Calculation,
    ): Calculation {
        val existingCalculation =
            calculationRepository.findById(id).orElse(null)
                ?: throw Exception("Calculation not found")

        if (existingCalculation.userId != calculation.userId) {
            throw Exception("User does not have permission to update this calculation")
        }

        return calculationRepository.save(calculation)
    }

    fun search(
        userId: UserId,
        query: String?,
        pageable: PageRequest,
    ): Page<Calculation> = calculationRepository.findByUserId(userId, pageable)

    fun duplicate(id: CalculationId, userId: UserId): Calculation {
        val originalCalculation = findById(id)
        if (originalCalculation.userId != userId) {
            throw Exception("User does not have permission to duplicate this calculation")
        }
        val duplicatedCalculation = originalCalculation.copy(
            id = UUID.randomUUID(),
            name = "Copy of ${originalCalculation.name}"
        )
        return create(duplicatedCalculation)
    }

    fun delete(id: CalculationId, userId: UserId) {
        val calculation = findById(id)
        if (calculation.userId != userId) {
            throw Exception("User does not have permission to delete this calculation")
        }
        calculationRepository.deleteById(id)
    }
}
