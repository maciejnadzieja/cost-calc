package com.costcalc.api.config

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.model.User
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.UUID

@Configuration
class WebConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }

    @Component
    class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean =
            parameter.getParameterAnnotation(CurrentUser::class.java) != null

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?,
        ): User {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.principal is Jwt) {
                val jwt = authentication.principal as Jwt
                val email = jwt.getClaimAsString("email")
                return User(UUID.nameUUIDFromBytes(email.toByteArray()), email)
            }
            throw RuntimeException("user not authenticated")
        }
    }
}
