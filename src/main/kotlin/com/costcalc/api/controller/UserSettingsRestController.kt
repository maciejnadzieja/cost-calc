package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.UserSettingsRequest
import com.costcalc.api.model.User
import com.costcalc.api.model.UserSettings
import com.costcalc.api.service.UserSettingsService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user-settings")
@PreAuthorize("isAuthenticated()")
class UserSettingsRestController(
    private val userSettingsService: UserSettingsService,
) {
    @GetMapping
    fun getUserSettings(
        @CurrentUser user: User,
    ): UserSettings = userSettingsService.getUserSettings(user)

    @PutMapping
    fun updateUserSettings(
        @CurrentUser user: User,
        @RequestBody userSettingsRequest: UserSettingsRequest,
    ): UserSettings {
        val userSettings =
            UserSettings(
                id = user.id,
                currency = userSettingsRequest.currency,
                footerText = userSettingsRequest.footerText,
            )

        return userSettingsService.updateUserSettings(user, userSettings)
    }
}
