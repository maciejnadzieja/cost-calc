package com.costcalc.api.controller

import com.costcalc.api.dto.UserSettingsForm
import com.costcalc.api.model.User
import com.costcalc.api.service.UserSettingsService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/settings")
class UserSettingsController(
    private val userSettingsService: UserSettingsService,
) {
    @GetMapping
    fun showSettings(
        model: Model,
        user: User,
    ): String {
        val settings = userSettingsService.getOrCreateUserSettings(user.id)

        model.addAttribute(
            "settings",
            UserSettingsForm(
                currency = settings.currency,
                themeColor = settings.themeColor,
                footerText = settings.footerText,
            ),
        )
        model.addAttribute("availableCurrencies", listOf("USD", "EUR", "GBP", "JPY", "PLN"))
        return "settings/form"
    }

    @PostMapping
    fun updateSettings(
        @ModelAttribute settingsForm: UserSettingsForm,
        user: User,
    ): String {
        userSettingsService.updateUserSettings(
            userId = user.id,
            currency = settingsForm.currency,
            themeColor = settingsForm.themeColor,
            footerText = settingsForm.footerText,
        )
        return "redirect:/settings"
    }
}
