package com.costcalc.api.config

import com.costcalc.api.model.User
import com.costcalc.api.model.UserSettings
import com.costcalc.api.service.UserSettingsService
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class ModelAttributeConfig(
    private val userSettingsService: UserSettingsService,
) {
    @ModelAttribute("userSettings")
    fun userSettings(user: User?): UserSettings? = user?.let { userSettingsService.getOrCreateUserSettings(user.id) }
} 
