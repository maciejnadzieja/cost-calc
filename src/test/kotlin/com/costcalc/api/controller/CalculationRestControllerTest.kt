
package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationStage
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.CalculationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

class CalculationRestControllerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var calculationRepository: CalculationRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        calculationRepository.deleteAll()
    }

    @Test
    fun `should return supported currencies`() {
        mockMvc.get("/api/calculations/currencies") {
            with(token())
        }.andExpect {
            status { isOk() }
            jsonPath("$[0]") { value("EUR") }
            jsonPath("$[1]") { value("PLN") }
            jsonPath("$[2]") { value("USD") }
        }
    }

    @Test
    fun `should handle calculation lifecycle`() {
        val userId = UUID.randomUUID()

        // Create
        val result = mockMvc.post("/api/calculations") {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson()
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { exists() }
        }.andReturn()

        val createdId = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        // Get by id
        mockMvc.get("/api/calculations/{id}", createdId) {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Test Calculation") }
        }

        // Search
        mockMvc.get("/api/calculations") {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].name") { value("Test Calculation") }
        }

        // Update
        mockMvc.put("/api/calculations/{id}", createdId) {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson(name = "Updated Calculation")
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Updated Calculation") }
        }

        // Duplicate
        val duplicateResult = mockMvc.post("/api/calculations/{id}/duplicate", createdId) {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { exists() }
            jsonPath("$.name") { value("Updated Calculation (copy)") }
        }.andReturn()

        val duplicatedId = objectMapper.readTree(duplicateResult.response.contentAsString).get("id").asText()

        // Delete duplicated
        mockMvc.delete("/api/calculations/{id}", duplicatedId) {
            with(token(userId))
        }.andExpect {
            status { isNoContent() }
        }

        // Delete
        mockMvc.delete("/api/calculations/{id}", createdId) {
            with(token(userId))
        }.andExpect {
            status { isNoContent() }
        }

        // Verify deletion
        mockMvc.get("/api/calculations/{id}", createdId) {
            with(token(userId))
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should not allow user to access calculations of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val calculation = createTestCalculation(userId = userId1)
        calculationRepository.save(calculation)

        mockMvc.get("/api/calculations/{id}", calculation.id) {
            with(token(userId2))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should return paged results`() {
        val userId = UUID.randomUUID()
        (1..15).forEach {
            calculationRepository.save(createTestCalculation(userId = userId, name = "Calculation $it"))
        }

        mockMvc.get("/api/calculations?page=0&size=10") {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(10) }
            jsonPath("$.totalPages") { value(2) }
            jsonPath("$.totalElements") { value(15) }
        }

        mockMvc.get("/api/calculations?page=1&size=10") {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(5) }
        }
    }

    @Test
    fun `should filter calculations by name`() {
        val userId = UUID.randomUUID()
        calculationRepository.save(createTestCalculation(userId, name = "Test Calculation 1"))
        calculationRepository.save(createTestCalculation(userId, name = "Test Calculation 2"))
        calculationRepository.save(createTestCalculation(userId, name = "Another Calculation"))

        mockMvc.get("/api/calculations?query=Test") {
            with(token(userId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(2) }
        }
    }

    @Test
    fun `should secure all routes`() {
        mockMvc.get("/api/calculations").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/calculations/currencies").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/calculations/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
        mockMvc.post("/api/calculations") {
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson()
        }.andExpect { status { isUnauthorized() } }
        mockMvc.put("/api/calculations/{id}", UUID.randomUUID()) {
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson()
        }.andExpect { status { isUnauthorized() } }
        mockMvc.delete("/api/calculations/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
        mockMvc.post("/api/calculations/{id}/duplicate", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
    }

    private fun token(
        userId: UserId = UUID.randomUUID(),
        email: String = "$userId@example.com",
    ) = jwt().jwt {
        it.claim("sub", userId.toString())
        it.claim("email", email)
    }

    private fun createTestCalculation(
        userId: UserId,
        name: String = "Test Calculation",
        description: String = "Description",
        stages: List<CalculationStage> = emptyList(),
        currency: String = "USD"
    ) = Calculation(
        id = UUID.randomUUID(),
        name = name,
        description = description,
        stages = stages,
        currency = currency,
        userId = userId
    )

    private fun createCalculationJson(
        name: String = "Test Calculation",
        description: String? = "Description",
        stages: List<CalculationStage> = emptyList(),
        currency: String = "USD"
    ): String {
        val stagesJson = objectMapper.writeValueAsString(stages)
        return """
        {
            "name": "$name",
            "description": ${description},
            "stages": $stagesJson,
            "currency": "$currency"
        }
        """.trimIndent()
    }
}
