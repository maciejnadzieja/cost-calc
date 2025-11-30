package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.UserId
import com.costcalc.api.model.UserSettings
import com.costcalc.api.repository.UserSettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.util.UUID

class UserSettingsRestControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userSettingsRepository: UserSettingsRepository

    @BeforeEach
    fun setUp() {
        userSettingsRepository.deleteAll()
    }

    @Test
    fun `should return default settings for new user`() {
        val userId = UUID.randomUUID()

        mockMvc
            .get("/api/user-settings") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.currency") { value("USD") }
                jsonPath("$.footerText") { value("") }
            }
    }

    @Test
    fun `should update user settings`() {
        val userId = UUID.randomUUID()

        val updateJson = """
            {
                "currency": "EUR",
                "footerText": "My Footer"
            }
        """.trimIndent()

        mockMvc
            .put("/api/user-settings") {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateJson
            }.andExpect {
                status { isOk() }
                jsonPath("$.currency") { value("EUR") }
                jsonPath("$.footerText") { value("My Footer") }
            }

        // Verify persistence
        mockMvc
            .get("/api/user-settings") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.currency") { value("EUR") }
                jsonPath("$.footerText") { value("My Footer") }
            }
    }

    private fun token(
        userId: UserId = UUID.randomUUID(),
        email: String = "$userId@example.com",
    ) = jwt().jwt {
        it.claim("sub", userId.toString())
        it.claim("email", email)
    }
}
