package com.costcalc.api.service

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.CalculationStage
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.CalculationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CalculationService(
    private val calculationRepository: CalculationRepository,
) {
    fun search(
        userId: UserId,
        query: String?,
        pageable: Pageable,
    ): Page<Calculation> =
        if (query.isNullOrBlank()) {
            calculationRepository.findByUserId(userId, pageable)
        } else {
            calculationRepository
                .findByUserIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    userId,
                    query,
                    query,
                    pageable,
                )
        }

    fun findByIdAndUserId(
        id: CalculationId,
        userId: UserId,
    ): Calculation =
        calculationRepository
            .findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Calculation not found")

    fun create(calculation: Calculation): Calculation = calculationRepository.save(calculation)

    fun duplicate(
        id: CalculationId,
        userId: UserId,
    ): Calculation = calculationRepository.save(findByIdAndUserId(id, userId).duplicate())

    fun update(
        id: UUID,
        name: String,
        description: String,
        stages: List<CalculationStage>,
        currency: String,
        userId: UserId,
    ): Calculation {
        val existingCalculation =
            calculationRepository
                .findByIdAndUserId(id, userId)
                ?: throw NoSuchElementException("Calculation not found")

        return calculationRepository.save(
            existingCalculation.update(
                name = name,
                description = description,
                stages = stages,
                currency = currency,
            ),
        )
    }

    fun delete(
        id: CalculationId,
        userId: UserId,
    ) {
        calculationRepository
            .findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Calculation not found")
        calculationRepository.deleteByIdAndUserId(id, userId)
    }
}
