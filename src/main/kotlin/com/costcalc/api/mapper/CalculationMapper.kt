package com.costcalc.api.mapper

import com.costcalc.api.dto.CalculationItemResponse
import com.costcalc.api.dto.CalculationResponse
import com.costcalc.api.dto.CalculationStageResponse
import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationItem
import com.costcalc.api.model.CalculationStage

fun Calculation.toResponse(): CalculationResponse =
    CalculationResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        currency = this.currency.code,
        stages = this.stages.map { it.toResponse() },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        totalAmount = this.totalAmount,
    )

fun CalculationStage.toResponse(): CalculationStageResponse =
    CalculationStageResponse(
        name = this.name,
        services = this.services.map { it.toResponse() },
        materials = this.materials.map { it.toResponse() },
        servicesTotalAmount = this.servicesTotalAmount,
        materialsTotalAmount = this.materialsTotalAmount,
        totalAmount = this.totalAmount,
    )

fun CalculationItem.toResponse(): CalculationItemResponse =
    CalculationItemResponse(
        name = this.name,
        quantity = this.quantity,
        unit = this.unit,
        unitPrice = this.unitPrice,
        type = this.type,
        amount = this.amount,
    )
