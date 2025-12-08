package com.costcalc.api.service

import com.costcalc.api.model.User
import com.costcalc.api.model.UserSettings
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.stereotype.Service

@Service
class UserSettingsService(
    private val userSettingsRepository: UserSettingsRepository,
    private val currencyConfig: com.costcalc.api.config.CurrencyConfig,
) {
    fun getUserSettings(user: User): UserSettings =
        userSettingsRepository.findById(user.id).orElseGet {
            val defaultCurrency = currencyConfig.defaultCurrency()
            val newUserSettings = UserSettings(id = user.id, currency = defaultCurrency)
            userSettingsRepository.save(newUserSettings)
        }

    fun updateUserSettings(
        user: User,
        userSettings: UserSettings,
    ): UserSettings {
        val existingSettings = getUserSettings(user)
        val updatedSettings =
            existingSettings.copy(
                currency = userSettings.currency,
                footerText = userSettings.footerText,
            )
        return userSettingsRepository.save(updatedSettings)
    }
}
