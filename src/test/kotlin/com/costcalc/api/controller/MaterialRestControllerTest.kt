
package com.costcalc.api.controller

import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.MaterialRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class MaterialRestControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var materialRepository: MaterialRepository

    @BeforeEach
    fun setUp() {
        materialRepository.deleteAll()
    }

    @Test
    fun `should not allow user to access materials of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val material =
            Material.create(
                name = "Test Material",
                description = "Test Description",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Test Category",
                userId = userId1,
            )
        materialRepository.save(material)

        // User 1 can access their own material
        mockMvc
            .get("/api/materials/{id}", material.id) {
                with(jwt(userId1, "email1@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Material") }
            }

        // User 2 cannot access User 1's material
        mockMvc
            .get("/api/materials/{id}", material.id) {
                with(jwt(userId2, "email2@example.com"))
            }.andExpect {
                status { isNotFound() }
            }
    }

    private fun jwt(
        userId: UserId,
        email: String,
    ) = jwt().jwt {
        it.claim("sub", userId.toString())
        it.claim("email", email)
    }
}
