package com.costcalc.api.controller

import com.costcalc.api.config.SecurityConfig
import com.costcalc.api.dto.AdminUser
import com.costcalc.api.dto.AdminUserPage
import com.costcalc.api.service.AdminUserService
import com.costcalc.api.service.DirectoryUnavailableException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AdminUserRestController::class)
@ContextConfiguration(classes = [SecurityConfig::class, AdminUserRestController::class])
class AdminUserRestControllerTest {
    @Autowired lateinit var mvc: MockMvc
    @MockitoBean lateinit var decoder: JwtDecoder
    @MockitoBean lateinit var users: AdminUserService

    @BeforeEach
    fun tokens() {
        listOf("admin", "user", "client-admin", "lowercase", "malformed").forEach {
            val builder = Jwt.withTokenValue(it).header("alg", "RS256").subject("human")
                .claim("resource_access", mapOf("client" to mapOf("roles" to listOf("ADMIN"))))
            when (it) {
                "admin" -> builder.claim("realm_access", mapOf("roles" to listOf("ADMIN")))
                "user" -> builder.claim("realm_access", mapOf("roles" to listOf("USER")))
                "lowercase" -> builder.claim("realm_access", mapOf("roles" to listOf("admin")))
                "malformed" -> builder.claim("realm_access", mapOf("roles" to "ADMIN"))
            }
            whenever(decoder.decode(it)).thenReturn(builder.build())
        }
        whenever(decoder.decode("invalid")).thenThrow(BadJwtException("Invalid test bearer"))
    }

    @Test
    fun `realm admin reads only approved columns`() {
        whenever(users.page(0)).thenReturn(AdminUserPage(listOf(AdminUser("never-logged-in", null, false)), 0, false))
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer admin"))
            .andExpect(status().isOk).andExpect(jsonPath("$.size").value(20))
            .andExpect(content().json("""{"content":[{"username":"never-logged-in","email":null,"enabled":false}],"number":0,"size":20,"hasNext":false}""", true))
    }

    @Test
    fun `rejected requests never invoke directory service`() {
        listOf("user", "client-admin", "lowercase", "malformed").forEach {
            mvc.perform(get("/api/admin/users").header("Authorization", "Bearer $it")).andExpect(status().isForbidden)
        }
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized)
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized)
        verifyNoInteractions(users)
    }

    @Test
    fun `invalid pages are rejected without directory calls`() {
        listOf("-1", "not-a-page", "2147483648").forEach {
            mvc.perform(get("/api/admin/users").param("page", it).header("Authorization", "Bearer admin"))
                .andExpect(status().isBadRequest).andExpect(jsonPath("$.code").value("INVALID_PAGE"))
        }
        verifyNoInteractions(users)
    }

    @Test
    fun `directory failure is safe and read-only contract rejects writes`() {
        whenever(users.page(0)).thenThrow(DirectoryUnavailableException())
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer admin"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(content().json("""{"code":"DIRECTORY_UNAVAILABLE","message":"User directory is temporarily unavailable"}""", true))
        mvc.perform(post("/api/admin/users").header("Authorization", "Bearer admin")).andExpect(status().isMethodNotAllowed)
    }
}
