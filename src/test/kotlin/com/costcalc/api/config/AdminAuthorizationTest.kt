package com.costcalc.api.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(AdminAuthorizationTest.ProbeController::class)
@Import(SecurityConfig::class, AdminAuthorizationTest.ProbeController::class)
@ContextConfiguration(classes = [SecurityConfig::class, AdminAuthorizationTest.ProbeController::class])
class AdminAuthorizationTest {
    @Autowired lateinit var mvc: MockMvc
    @MockitoBean lateinit var decoder: JwtDecoder

    @RestController
    class ProbeController {
        @GetMapping("/api/admin/probe", "/api/ordinary/probe")
        fun probe() = "ok"
    }

    @BeforeEach
    fun tokens() {
        whenever(decoder.decode("admin")).thenReturn(token(mapOf("roles" to listOf("ADMIN"))))
        whenever(decoder.decode("user")).thenReturn(token(mapOf("roles" to listOf("USER"))))
        whenever(decoder.decode("client-admin")).thenReturn(token())
        whenever(decoder.decode("invalid")).thenThrow(BadJwtException("Invalid test token"))
    }

    private fun token(realm: Any? = null): Jwt {
        val builder = Jwt.withTokenValue("test").header("alg", "RS256").subject("human")
            .claim("resource_access", mapOf("cost-calc-client" to mapOf("roles" to listOf("ADMIN"))))
        if (realm != null) builder.claim("realm_access", realm)
        return builder.build()
    }

    @Test
    fun `real bearer conversion admits realm ADMIN only`() {
        mvc.perform(get("/api/admin/probe").header("Authorization", "Bearer admin")).andExpect(status().isOk)
        listOf("user", "client-admin").forEach {
            mvc.perform(get("/api/admin/probe").header("Authorization", "Bearer $it")).andExpect(status().isForbidden)
        }
    }

    @Test
    fun `missing and invalid bearer are unauthenticated`() {
        mvc.perform(get("/api/admin/probe")).andExpect(status().isUnauthorized)
        mvc.perform(get("/api/admin/probe").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `ordinary resources retain authenticated access`() {
        mvc.perform(get("/api/ordinary/probe").header("Authorization", "Bearer user")).andExpect(status().isOk)
        mvc.perform(get("/api/ordinary/probe")).andExpect(status().isUnauthorized)
    }
}
