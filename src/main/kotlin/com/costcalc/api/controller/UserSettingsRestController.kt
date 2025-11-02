package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.UserSettingsRequest
import com.costcalc.api.model.User
import com.costcalc.api.model.UserSettings
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/user-settings")
@PreAuthorize("isAuthenticated()")
class UserSettingsRestController(
    private val userSettingsRepository: UserSettingsRepository,
) {
    @GetMapping
    fun getUserSettings(
        @CurrentUser user: User,
    ): UserSettings =
        userSettingsRepository.findById(user.id).orElseGet {
            val newUserSettings = UserSettings(id = user.id)
            userSettingsRepository.save(newUserSettings)
        }

    @PutMapping
    fun updateUserSettings(
        @CurrentUser user: User,
        @RequestBody userSettingsRequest: UserSettingsRequest,
    ): UserSettings {
        val userSettings =
            userSettingsRepository.findById(user.id).orElseGet {
                UserSettings(id = user.id)
            }

        val updatedSettings =
            userSettings.copy(
                currency = userSettingsRequest.currency,
                footerText = userSettingsRequest.footerText,
                updatedAt = Instant.now(),
            )

        return userSettingsRepository.save(updatedSettings)
    }
}
