package com.costcalc.api.config

import com.costcalc.api.config.auth.UserArgumentResolver
import com.costcalc.api.model.WorkUnit
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val userArgumentResolver: UserArgumentResolver,
    private val messageSource: MessageSource,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userArgumentResolver)
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToWorkUnitConverter(messageSource))
    }
}

class StringToWorkUnitConverter(
    private val messageSource: MessageSource,
) : Converter<String, WorkUnit> {
    override fun convert(source: String): WorkUnit =
        try {
            // First try to convert by enum name
            WorkUnit.valueOf(source.uppercase())
        } catch (e: IllegalArgumentException) {
            // If that fails, try to find by label or symbol
            val locale = LocaleContextHolder.getLocale()
            WorkUnit.entries.find { unit ->
                val label = messageSource.getMessage("service.unit.${unit.name.lowercase()}.label", null, unit.name, locale)
                val symbol = messageSource.getMessage("service.unit.${unit.name.lowercase()}.symbol", null, unit.name, locale)
                source == label || source == symbol
            } ?: throw IllegalArgumentException("Invalid work unit: $source")
        }
}
