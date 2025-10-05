package com.costcalc.api.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.Locale

@Configuration
class LocaleConfig : WebMvcConfigurer {
    
    class SmartLocaleResolver : SessionLocaleResolver() {
        override fun resolveLocale(request: HttpServletRequest): Locale {
            // First try to get locale from session
            val sessionLocale = request.session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME) as? Locale
            if (sessionLocale != null) {
                return sessionLocale
            }
            
            // If no session locale, try Accept-Language header
            val acceptLanguageHeader = request.getHeader("Accept-Language")
            if (!acceptLanguageHeader.isNullOrBlank()) {
                // Parse the Accept-Language header
                val locale = acceptLanguageHeader.split(",")
                    .firstOrNull()
                    ?.split(";")
                    ?.firstOrNull()
                    ?.trim()
                    ?.let { 
                        if (it.contains("-")) {
                            val parts = it.split("-")
                            Locale(parts[0].lowercase())
                        } else {
                            Locale(it.lowercase())
                        }
                    }
                
                if (locale != null) {
                    return when (locale.language) {
                        "pl", "en" -> locale // Add other supported languages here
                        else -> defaultLocale ?: Locale.ENGLISH
                    }
                }
            }
            
            return defaultLocale ?: Locale.ENGLISH
        }
    }
    
    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = SmartLocaleResolver()
        resolver.setDefaultLocale(Locale.ENGLISH)
        return resolver
    }
    
    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor {
        val interceptor = LocaleChangeInterceptor()
        interceptor.paramName = "lang"
        return interceptor
    }
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
    }
} 