package com.costcalc.api.model

@JvmInline
value class Currency(
    val code: String,
) {
    init {
        require(code.matches(Regex("^[A-Z]{3}$"))) { "Currency code must be 3 uppercase letters" }
    }

    override fun toString(): String = code
}
