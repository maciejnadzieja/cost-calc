package com.costcalc.api.validation

import com.costcalc.api.config.CurrencyConfig
import com.costcalc.api.model.Currency
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class SupportedCurrencyValidatorTest {
    private val currencyConfig = CurrencyConfig(listOf("USD", "EUR", "PLN"))
    private val validator = SupportedCurrencyValidator(currencyConfig)
    private val context = mock<ConstraintValidatorContext>()

    @Test
    fun `isValid should return true for supported currency`() {
        assertTrue(validator.isValid(Currency("USD"), context))
        assertTrue(validator.isValid(Currency("EUR"), context))
    }

    @Test
    fun `isValid should return false for unsupported currency`() {
        assertFalse(validator.isValid(Currency("GBP"), context))
        assertFalse(validator.isValid(Currency("JPY"), context))
    }
}
