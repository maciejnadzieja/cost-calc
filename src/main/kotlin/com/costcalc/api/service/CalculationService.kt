package com.costcalc.api.service

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.CalculationRepository
import org.springframework.stereotype.Service

@Service
class CalculationService(
    private val calculationRepository: CalculationRepository,
) {
    fun getAllByUserId(userId: UserId): List<Calculation> = calculationRepository.findByUserId(userId)

    fun create(calculation: Calculation): Calculation = calculationRepository.save(calculation)

    fun findById(id: CalculationId): Calculation {
        return calculationRepository.findById(id).orElse(null)
            ?: throw Exception("Calculation not found")
    }

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
}
