package com.costcalc.api.service

import com.costcalc.api.model.UserId
import com.costcalc.api.model.UserSettings
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserSettingsService(
    private val userSettingsRepository: UserSettingsRepository,
) {
    fun getOrCreateUserSettings(userId: UserId): UserSettings =
        userSettingsRepository.findById(userId).orElseGet { createDefaultSettings(userId) }

    private fun createDefaultSettings(userId: UserId): UserSettings {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val settings =
            UserSettings(
                id = userId,
                currency = "USD",
                themeColor = "#007bff",
                footerText = "",
                createdAt = now,
                updatedAt = now,
            )
        return userSettingsRepository.save(settings)
    }

    fun updateUserSettings(
        userId: UserId,
        currency: String?,
        themeColor: String?,
        footerText: String?,
    ): UserSettings {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val settings =
            userSettingsRepository.findById(userId).orElseThrow {
                RuntimeException("User Not Found")
            }

        val updatedSettings =
            settings?.copy(
                currency = currency ?: settings.currency,
                themeColor = themeColor ?: settings.themeColor,
                footerText = footerText ?: settings.footerText,
                updatedAt = now,
            )
                ?: createDefaultSettings(userId)

        return userSettingsRepository.save(updatedSettings)
    }
}
