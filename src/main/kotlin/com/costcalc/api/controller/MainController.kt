package com.costcalc.api.controller

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController {

    @GetMapping("/")
    fun mainPage(authentication: Authentication?): String {
        if (authentication != null && authentication.isAuthenticated) {
            return "redirect:/dashboard"
        }
        return "main"
    }
}
