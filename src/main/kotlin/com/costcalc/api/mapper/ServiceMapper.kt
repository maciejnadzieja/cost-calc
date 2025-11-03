package com.costcalc.api.mapper

import com.costcalc.api.dto.ServiceResponse
import com.costcalc.api.model.Service

fun Service.toResponse(): ServiceResponse =
    ServiceResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        workUnit = this.workUnit,
    )
