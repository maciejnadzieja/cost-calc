package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.MaterialRepository
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
import java.math.BigDecimal
import java.util.UUID

class MaterialRestControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var materialRepository: MaterialRepository

    @BeforeEach
    fun setUp() {
        materialRepository.deleteAll()
    }

    @Test
    fun `should return all units`() {
        mockMvc
            .get("/api/materials/units") {
                with(token())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should handle material lifecycle`() {
        val userId = UUID.randomUUID()

        // Create
        val result =
            mockMvc
                .post("/api/materials") {
                    with(token(userId))
                    contentType = MediaType.APPLICATION_JSON
                    content = createMaterialJson()
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { exists() }
                }.andReturn()

        val createdMaterialId =
            com.fasterxml.jackson.databind
                .ObjectMapper()
                .readTree(result.response.contentAsString)
                .get("id")
                .asText()

        // Get by id
        mockMvc
            .get("/api/materials/{id}", createdMaterialId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Material") }
            }

        // Search
        mockMvc
            .get("/api/materials") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Test Material") }
            }

        // Delete
        mockMvc
            .delete("/api/materials/{id}", createdMaterialId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
            }

        // Verify deletion
        mockMvc
            .get("/api/materials/{id}", createdMaterialId) {
                with(token(userId))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should not allow user to access materials of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val material = createTestMaterial(userId = userId1)
        materialRepository.save(material)

        // User 1 can access their own material
        mockMvc
            .get("/api/materials/{id}", material.id) {
                with(token(userId1))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Material") }
            }

        // User 2 cannot access User 1's material
        mockMvc
            .get("/api/materials/{id}", material.id) {
                with(token(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should allow user to update their own material`() {
        val userId = UUID.randomUUID()
        val material = createTestMaterial(userId = userId)
        materialRepository.save(material)

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateMaterialJson()
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Updated Material") }
            }
    }

    @Test
    fun `should not allow user to update another user's material`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val material = createTestMaterial(userId = userId1)
        materialRepository.save(material)

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(token(userId2))
                contentType = MediaType.APPLICATION_JSON
                content = updateMaterialJson()
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input on update`() {
        val userId = UUID.randomUUID()
        val material = createTestMaterial(userId = userId)
        materialRepository.save(material)

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateMaterialJson(price = -20.0)
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should allow user to delete their own material`() {
        val userId = UUID.randomUUID()
        val material = createTestMaterial(userId = userId)
        materialRepository.save(material)

        mockMvc
            .delete("/api/materials/{id}", material.id) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should not allow user to delete another user's material`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val material = createTestMaterial(userId = userId1)
        materialRepository.save(material)

        mockMvc
            .delete("/api/materials/{id}", material.id) {
                with(token(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input price`() {
        mockMvc
            .post("/api/materials") {
                with(token())
                contentType = MediaType.APPLICATION_JSON
                content = createMaterialJson(price = -10.0)
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return bad request for invalid input name`() {
        mockMvc
            .post("/api/materials") {
                with(token())
                contentType = MediaType.APPLICATION_JSON
                content = createMaterialJson(name = "")
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should secure all routes`() {
        mockMvc.get("/api/materials").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/materials/units").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/materials/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
        mockMvc
            .post("/api/materials") {
                contentType = MediaType.APPLICATION_JSON
                content = createMaterialJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc
            .put("/api/materials/{id}", UUID.randomUUID()) {
                contentType = MediaType.APPLICATION_JSON
                content = updateMaterialJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc.delete("/api/materials/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `should return paged results`() {
        val userId = UUID.randomUUID()
        (1..15).forEach {
            materialRepository.save(createTestMaterial(userId = userId, name = "Material $it"))
        }

        // First page
        mockMvc
            .get("/api/materials?page=0&size=10") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(10) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }

        // Second page
        mockMvc
            .get("/api/materials?page=1&size=10") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(5) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }
    }

    @Test
    fun `should return bad request for invalid paging params`() {
        mockMvc
            .get("/api/materials?page=-1&size=10") {
                with(token())
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/materials?page=0&size=-10") {
                with(token())
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should filter materials by name`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId, name = "Test Material 1"))
        materialRepository.save(createTestMaterial(userId, name = "Test Material 2"))
        materialRepository.save(createTestMaterial(userId, name = "Another Material"))

        mockMvc
            .get("/api/materials?query=Test") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
            }
    }

    private fun token(
        userId: UserId = UUID.randomUUID(),
        email: String = "$userId@example.com",
    ) = jwt().jwt {
        it.claim("sub", userId.toString())
        it.claim("email", email)
    }

    private fun createTestMaterial(
        userId: UserId,
        name: String = "Test Material",
        description: String = "$name Description",
        price: BigDecimal = BigDecimal.TEN,
        unit: MaterialUnit = MaterialUnit.KILOGRAM,
        category: String = "Test Category",
    ) = Material.create(
        name = name,
        description = description,
        price = price,
        unit = unit,
        category = category,
        userId = userId,
    )

    private fun createMaterialJson(
        name: String = "Test Material",
        description: String = "$name Description",
        price: Double = 10.0,
        unit: MaterialUnit = MaterialUnit.KILOGRAM,
        category: String = "Test Category",
    ) = """
        {
            "name": "$name",
            "description": "$description",
            "price": $price,
            "unit": "$unit",
            "category": "$category"
        }
        """.trimIndent()

    private fun updateMaterialJson(
        name: String = "Updated Material",
        description: String = "Updated Description",
        price: Double = 20.0,
        unit: MaterialUnit = MaterialUnit.GRAM,
        category: String = "Updated Category",
    ) = """
        {
            "name": "$name",
            "description": "$description",
            "price": $price,
            "unit": "$unit",
            "category": "$category"
        }
        """.trimIndent()
}
