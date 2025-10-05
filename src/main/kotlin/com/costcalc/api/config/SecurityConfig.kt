package com.costcalc.api.config

import com.costcalc.api.repository.UserRepository
import com.costcalc.api.service.UserSettingsService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import java.time.Instant
import java.util.UUID
import com.costcalc.api.model.User as AppUser

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userRepository: UserRepository,
    private val userSettingsService: UserSettingsService,
    private val passwordEncoder: PasswordEncoder,
) {
    @Bean
    fun oauth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val delegate = DefaultOAuth2UserService()

        return OAuth2UserService { userRequest ->
            val oAuth2User = delegate.loadUser(userRequest)
            val email =
                oAuth2User.getAttribute<String>("email")
                    ?: throw IllegalStateException("Email not found from OAuth2 provider")

            var user = userRepository.findByEmail(email)
            if (user == null) {
                user =
                    AppUser(
                        id = UUID.randomUUID(),
                        email = email,
                        password = passwordEncoder.encode(""), // Empty password for OAuth users
                        firstName = oAuth2User.getAttribute<String>("given_name") ?: "",
                        lastName = oAuth2User.getAttribute<String>("family_name") ?: "",
                        roles = setOf("ROLE_USER"),
                        isEmailVerified = true,
                        lastLoginAt = Instant.now(),
                    )
                userRepository.save(user)
                // Create default user settings
                userSettingsService.getOrCreateUserSettings(user.id)
            } else {
                // Update last login time
                userRepository.save(user.copy(lastLoginAt = Instant.now()))
            }

            oAuth2User
        }
    }

    @Bean
    fun customAuthenticationSuccessHandler(): AuthenticationSuccessHandler =
        object : SimpleUrlAuthenticationSuccessHandler() {
            override fun onAuthenticationSuccess(
                request: HttpServletRequest,
                response: HttpServletResponse,
                authentication: Authentication,
            ) {
                val email =
                    when (val principal = authentication.principal) {
                        is OAuth2User -> principal.attributes["email"] as String
                        is UserDetails -> principal.username
                        else -> principal.toString()
                    }
//                userSettingsService.getOrCreateUserSettings(email)
                super.setDefaultTargetUrl("/dashboard")
                super.onAuthenticationSuccess(request, response, authentication)
            }
        }

        @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/", permitAll)
                authorize("/login", permitAll)
                authorize("/api/**", authenticated)
                authorize("/admin/**", hasRole("ADMIN"))
                authorize("/public/**", permitAll)
                authorize("/css/**", permitAll)
                authorize("/js/**", permitAll)
                authorize("/images/**", permitAll)
                authorize("/dashboard", authenticated)
                authorize("/**", authenticated)
            }
            formLogin {
                loginPage = "/login"
                defaultSuccessUrl("/dashboard", true)
                authenticationSuccessHandler = customAuthenticationSuccessHandler()
                permitAll()
            }
            oauth2Login {
                loginPage = "/login"
                userInfoEndpoint {
                    userService = oauth2UserService()
                }
                defaultSuccessUrl("/dashboard", true)
            }
            logout {
                logoutSuccessUrl = "/"
                permitAll()
            }
            csrf {
                disable()
            }
        }
        return http.build()
    }
}
