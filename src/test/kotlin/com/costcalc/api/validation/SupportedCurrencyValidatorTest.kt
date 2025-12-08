package com.costcalc.api.validation

import com.costcalc.api.config.CurrencyConfig
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class SupportedCurrencyValidatorTest {
    private val currencyConfig = mock<CurrencyConfig>()
    private val validator = SupportedCurrencyValidator(currencyConfig)
    private val context = mock<ConstraintValidatorContext>()

    @Test
    fun `isValid should return true for supported currency`() {
        whenever(currencyConfig.supportedCurrencies).thenReturn(listOf("USD", "EUR", "PLN"))

        assertTrue(validator.isValid("USD", context))
        assertTrue(validator.isValid("EUR", context))
    }

    @Test
    fun `isValid should return false for unsupported currency`() {
        whenever(currencyConfig.supportedCurrencies).thenReturn(listOf("USD", "EUR", "PLN"))

        assertFalse(validator.isValid("GBP", context))
        assertFalse(validator.isValid("JPY", context))
    }

    @Test
    fun `isValid should return true for null`() {
        assertTrue(validator.isValid(null, context))
    }
}
