package com.costcalc.api.service

import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.CalculationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CalculationService(
    private val calculationRepository: CalculationRepository,
) {
    fun search(
        userId: UserId,
        query: String?,
        pageable: Pageable,
    ): Page<Calculation> = calculationRepository.findByUserId(userId, pageable)

    fun findById(id: CalculationId): Calculation =
        calculationRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Calculation not found") }

    fun create(calculation: Calculation): Calculation = calculationRepository.save(calculation)

    fun duplicate(
        id: CalculationId,
        userId: UserId,
    ): Calculation {
        val original = findById(id)
        val duplicated =
            original.copy(
                id = CalculationId.randomUUID(),
                name = "Copy of ${original.name}",
                userId = userId,
            )
        return calculationRepository.save(duplicated)
    }

    fun update(
        id: CalculationId,
        calculation: Calculation,
    ): Calculation = calculationRepository.save(calculation)

    fun delete(
        id: CalculationId,
        userId: UserId,
    ) {
        calculationRepository.deleteByIdAndUserId(id, userId)
    }
}
