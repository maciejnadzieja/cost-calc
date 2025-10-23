package com.costcalc.api.model

import org.springframework.data.annotation.Id
import java.util.UUID

typealias UserId = UUID

data class User(
    @Id
    val id: UserId,
    val email: String,
)
