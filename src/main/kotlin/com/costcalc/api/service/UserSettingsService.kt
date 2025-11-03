package com.costcalc.api.service

import com.costcalc.api.model.User
import com.costcalc.api.model.UserSettings
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.stereotype.Service

@Service
class UserSettingsService(
    private val userSettingsRepository: UserSettingsRepository,
) {
    fun getUserSettings(user: User): UserSettings =
        userSettingsRepository.findById(user.id).orElseGet {
            val newUserSettings = UserSettings(id = user.id)
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
