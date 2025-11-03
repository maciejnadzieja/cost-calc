package com.costcalc.api.mapper

import com.costcalc.api.dto.MaterialResponse
import com.costcalc.api.model.Material

fun Material.toResponse(): MaterialResponse =
    MaterialResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        unit = this.unit,
        category = this.category,
    )
