
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
        mockMvc
            .get("/api/calculations/currencies") {
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
        val result =
            mockMvc
                .post("/api/calculations") {
                    with(token(userId))
                    contentType = MediaType.APPLICATION_JSON
                    content = createCalculationJson()
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { exists() }
                }.andReturn()

        val createdId = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        // Get by id
        mockMvc
            .get("/api/calculations/{id}", createdId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Calculation") }
            }

        // Search
        mockMvc
            .get("/api/calculations") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Test Calculation") }
            }

        // Update
        mockMvc
            .put("/api/calculations/{id}", createdId) {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = createCalculationJson(name = "Updated Calculation")
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Updated Calculation") }
            }

        // Duplicate
        val duplicateResult =
            mockMvc
                .post("/api/calculations/{id}/duplicate", createdId) {
                    with(token(userId))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { exists() }
                    jsonPath("$.name") { value("Copy of Updated Calculation") }
                }.andReturn()

        val duplicatedId = objectMapper.readTree(duplicateResult.response.contentAsString).get("id").asText()

        // Delete duplicated
        mockMvc
            .delete("/api/calculations/{id}", duplicatedId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
            }

        // Delete
        mockMvc
            .delete("/api/calculations/{id}", createdId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
            }

        // Verify deletion
        mockMvc
            .get("/api/calculations/{id}", createdId) {
                with(token(userId))
            }.andExpect {
                status { isNotFound() }
            }
        mockMvc
            .get("/api/calculations/{id}", duplicatedId) {
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

        mockMvc
            .get("/api/calculations/{id}", calculation.id) {
                with(token(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should not allow user to delete calculation of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val calculation = createTestCalculation(userId = userId1)
        calculationRepository.save(calculation)

        mockMvc
            .delete("/api/calculations/{id}", calculation.id) {
                with(token(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should not allow user to update calculation of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val calculation = createTestCalculation(userId = userId1)

        calculationRepository.save(calculation)
        mockMvc
            .put("/api/calculations/{id}", calculation.id) {
                with(token(userId2))
                contentType = MediaType.APPLICATION_JSON
                content = createCalculationJson()
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request in case of stage with wrong items`() {
        val calculationWithInvalidItem =
            """
            {
                "name": "Test",
                "currency": "USD",
                "stages": [
                    {
                        "name": "Stage 1",
                        "services": [
                            { "name": "Service 1", "unitPrice": "10", "quantity": "0", "unit": "HOUR", "type": "SERVICE" }
                        ],
                        "materials": [
                            { "name": "Material 1", "unitPrice": "10", "quantity": "1", "unit": "KILOGRAM", "type": "MATERIAL" }
                        ]
                    }
                ]
            }
            """.trimIndent()

        mockMvc
            .post("/api/calculations") {
                with(token())
                contentType = MediaType.APPLICATION_JSON
                content = calculationWithInvalidItem
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return paged results`() {
        val userId = UUID.randomUUID()
        (1..15).forEach {
            calculationRepository.save(createTestCalculation(userId = userId, name = "Calculation $it"))
        }

        mockMvc
            .get("/api/calculations?page=0&size=10") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(10) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }

        mockMvc
            .get("/api/calculations?page=1&size=10") {
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

        mockMvc
            .get("/api/calculations?query=Test") {
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
        mockMvc
            .post("/api/calculations") {
                contentType = MediaType.APPLICATION_JSON
                content = createCalculationJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc
            .put("/api/calculations/{id}", UUID.randomUUID()) {
                contentType = MediaType.APPLICATION_JSON
                content = createCalculationJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc.delete("/api/calculations/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
        mockMvc.post("/api/calculations/{id}/duplicate", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `should calculate sums correctly after update`() {
        val userId = UUID.randomUUID()
        
        // Create initial calculation
        val result = mockMvc.post("/api/calculations") {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson()
        }.andReturn()
        
        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        // Update with items
        val updateJson = """
            {
                "name": "Updated Calculation",
                "description": "Description",
                "currency": "USD",
                "stages": [
                    {
                        "name": "Stage 1",
                        "services": [
                            { "name": "Service 1", "unitPrice": 100.00, "quantity": 2, "unit": "HOUR", "type": "SERVICE" }
                        ],
                        "materials": [
                            { "name": "Material 1", "unitPrice": 50.00, "quantity": 3, "unit": "PIECE", "type": "MATERIAL" }
                        ]
                    }
                ]
            }
        """.trimIndent()

        mockMvc.put("/api/calculations/{id}", id) {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = updateJson
        }.andExpect {
            status { isOk() }
            // Service: 100 * 2 = 200
            // Material: 50 * 3 = 150
            // Stage Total: 200 + 150 = 350
            // Calculation Total: 350
            jsonPath("$.stages[0].servicesTotalAmount") { value(200.0) }
            jsonPath("$.stages[0].materialsTotalAmount") { value(150.0) }
            jsonPath("$.stages[0].totalAmount") { value(350.0) }
            jsonPath("$.totalAmount") { value(350.0) }
        }

        // Update removing material
        val updateRemoveItemJson = """
            {
                "name": "Updated Calculation",
                "description": "Description",
                "currency": "USD",
                "stages": [
                    {
                        "name": "Stage 1",
                        "services": [
                            { "name": "Service 1", "unitPrice": 100.00, "quantity": 2, "unit": "HOUR", "type": "SERVICE" }
                        ],
                        "materials": []
                    }
                ]
            }
        """.trimIndent()

        mockMvc.put("/api/calculations/{id}", id) {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = updateRemoveItemJson
        }.andExpect {
            status { isOk() }
            // Service: 100 * 2 = 200
            // Material: 0
            // Stage Total: 200
            // Calculation Total: 200
            jsonPath("$.stages[0].servicesTotalAmount") { value(200.0) }
            jsonPath("$.stages[0].materialsTotalAmount") { value(0.0) }
            jsonPath("$.stages[0].totalAmount") { value(200.0) }
            jsonPath("$.totalAmount") { value(200.0) }
        }
    }

    @Test
    fun `should ignore provided totals and recalculate`() {
        val userId = UUID.randomUUID()
        
        // Create initial calculation
        val result = mockMvc.post("/api/calculations") {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = createCalculationJson()
        }.andReturn()
        
        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        // Update with items AND incorrect totals
        // Service: 100 * 1 = 100
        // Provided totals: 999 (should be ignored)
        val updateJson = """
            {
                "name": "Updated Calculation",
                "description": "Description",
                "currency": "USD",
                "stages": [
                    {
                        "name": "Stage 1",
                        "services": [
                            { 
                                "name": "Service 1", 
                                "unitPrice": 100.00, 
                                "quantity": 1, 
                                "unit": "HOUR", 
                                "type": "SERVICE",
                                "amount": 999.0 
                            }
                        ],
                        "materials": [],
                        "servicesTotalAmount": 999.0,
                        "materialsTotalAmount": 999.0,
                        "totalAmount": 999.0
                    }
                ],
                "totalAmount": 999.0
            }
        """.trimIndent()

        mockMvc.put("/api/calculations/{id}", id) {
            with(token(userId))
            contentType = MediaType.APPLICATION_JSON
            content = updateJson
        }.andExpect {
            status { isOk() }
            // Should be 100, not 999
            jsonPath("$.stages[0].servicesTotalAmount") { value(100.0) }
            jsonPath("$.stages[0].totalAmount") { value(100.0) }
            jsonPath("$.totalAmount") { value(100.0) }
        }
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
        currency: String = "USD",
    ) = Calculation(
        id = UUID.randomUUID(),
        name = name,
        description = description,
        stages = stages,
        currency = currency,
        userId = userId,
    )

    private fun createCalculationJson(
        name: String = "Test Calculation",
        description: String? = "Description",
        stages: List<CalculationStage> = emptyList(),
        currency: String = "USD",
    ): String {
        val stagesJson = objectMapper.writeValueAsString(stages)
        return """
            {
                "name": "$name",
                "description": "$description",
                "stages": $stagesJson,
                "currency": "$currency"
            }
            """.trimIndent()
    }
}
