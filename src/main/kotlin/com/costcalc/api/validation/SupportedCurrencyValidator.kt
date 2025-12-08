package com.costcalc.api.validation

import com.costcalc.api.config.CurrencyConfig
import com.costcalc.api.model.Currency
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SupportedCurrencyValidator(
    private val currencyConfig: CurrencyConfig,
) : ConstraintValidator<SupportedCurrency, Currency> {
    override fun isValid(
        value: Currency,
        context: ConstraintValidatorContext,
    ): Boolean = currencyConfig.supportedCurrencies.contains(value.code)
}
