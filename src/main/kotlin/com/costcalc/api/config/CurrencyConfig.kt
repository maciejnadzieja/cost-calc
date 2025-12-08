package com.costcalc.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "costcalc.config")
data class CurrencyConfig(
    val supportedCurrencies: List<String>,
) {
    fun defaultCurrency(): String = supportedCurrencies.first()
}
