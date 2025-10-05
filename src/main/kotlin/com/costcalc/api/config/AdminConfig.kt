package com.costcalc.api.config

import com.costcalc.api.model.User
import com.costcalc.api.repository.UserRepository
import com.costcalc.api.service.UserSettingsService
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@ConfigurationProperties(prefix = "app.admin")
data class AdminProperties
    @ConstructorBinding
    constructor(
        val email: String,
        val password: String,
    )

@Configuration
@EnableConfigurationProperties(AdminProperties::class)
class AdminConfig(
    private val userRepository: UserRepository,
    private val userSettingsService: UserSettingsService,
    private val passwordEncoder: PasswordEncoder,
    private val adminProperties: AdminProperties,
) {
    @PostConstruct
    fun initializeAdmin() {
        if (!userRepository.existsByEmail(adminProperties.email)) {
            val admin =
                User(
                    id = UUID(0, 0),
                    email = adminProperties.email,
                    password = passwordEncoder.encode(adminProperties.password),
                    firstName = "Admin",
                    lastName = "User",
                    roles = setOf("ROLE_ADMIN", "ROLE_USER"),
                    isEnabled = true,
                    isEmailVerified = true,
                    isAccountNonExpired = true,
                    isAccountNonLocked = true,
                    isCredentialsNonExpired = true,
                )
            userRepository.save(admin)
            userSettingsService.getOrCreateUserSettings(admin.id)
        }
    }
}
