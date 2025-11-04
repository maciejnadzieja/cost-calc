package com.costcalc.api.config

import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder

@TestConfiguration
class TestSecurityConfig {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return mock()
    }
}
