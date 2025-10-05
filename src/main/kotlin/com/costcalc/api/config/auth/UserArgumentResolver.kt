package com.costcalc.api.config.auth

import com.costcalc.api.model.User
import com.costcalc.api.repository.UserRepository
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.stereotype.Component
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.authentication.AnonymousAuthenticationToken

@Component
class UserArgumentResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == User::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val authentication = SecurityContextHolder.getContext().authentication
        
        // Return null for anonymous users
        if (authentication is AnonymousAuthenticationToken) {
            return null
        }
        
        val email = when (val principal = authentication.principal) {
            is OAuth2User -> principal.attributes["email"] as String
            is UserDetails -> principal.username
            else -> principal.toString()
        }
        
        return userRepository.findByEmail(email)
            ?: throw IllegalStateException("User $email not found")
    }
} 