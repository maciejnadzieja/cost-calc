package com.costcalc.api.validation

import com.costcalc.api.config.CurrencyConfig
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SupportedCurrencyValidator(
    private val currencyConfig: CurrencyConfig,
) : ConstraintValidator<SupportedCurrency, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true // Let @NotNull or @NotBlank handle nulls
        }
        return currencyConfig.supportedCurrencies.contains(value)
    }
}
