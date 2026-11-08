package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.Material
import com.costcalc.api.model.MaterialUnit
import com.costcalc.api.model.UserId
import com.costcalc.api.repository.MaterialRepository
import com.costcalc.api.migration.BackfillSearchFieldsChangeUnit
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.data.mongodb.core.MongoTemplate
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

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

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

    @Test
    fun `should find a material by a fragment inside its name or description`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId, name = "Instalacja", description = ""))
        materialRepository.save(createTestMaterial(userId, name = "Other", description = "Montaż instalacji"))

        mockMvc
            .get("/api/materials?query=stal") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.page.totalElements") { value(2) }
            }
    }

    @Test
    fun `should find materials with or without Polish accents in either direction`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId, name = "Łódź (a+b)", description = ""))
        materialRepository.save(createTestMaterial(userId, name = "Lodz", description = ""))

        for (query in listOf("lodz", "Łódź")) {
            mockMvc
                .get("/api/materials") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(2) }
                    jsonPath("$.page.totalElements") { value(2) }
                }
        }

        mockMvc
            .get("/api/materials") {
                with(token(userId))
                param("query", "lodz (a+b)")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].name") { value("Łódź (a+b)") }
            }
    }

    @Test
    fun `should match every Polish letter in paged user scoped material search`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId = userId, name = "ZAŻÓŁĆ GĘŚLĄ JAŹŃ", description = ""))
        materialRepository.save(createTestMaterial(userId = userId, name = "Other", description = "Zazolc gesla jazn"))
        materialRepository.save(createTestMaterial(userId = UUID.randomUUID(), name = "Zażółć gęślą jaźń", description = ""))

        for (query in listOf("zazolc gesla jazn", "ZAŻÓŁĆ GĘŚLĄ JAŹŃ")) {
            for (page in 0..1) {
                mockMvc
                    .get("/api/materials") {
                        with(token(userId))
                        param("query", query)
                        param("page", page.toString())
                        param("size", "1")
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content.length()") { value(1) }
                        jsonPath("$.page.totalElements") { value(2) }
                        jsonPath("$.page.totalPages") { value(2) }
                    }
            }
        }
    }

    @Test
    fun `should match accents from other languages in material names and descriptions`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId = userId, name = "São Paulo", description = "İstanbul"))

        for (query in listOf("sao", "istanbul")) {
            mockMvc
                .get("/api/materials") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content[0].name") { value("São Paulo") }
                }
        }
    }

    @Test
    fun `should backfill search fields for an existing material`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId = userId, name = "Łódź", description = "Café"))
        mongoTemplate.db.getCollection("materials").updateOne(
            Filters.eq("name", "Łódź"),
            Updates.combine(Updates.unset("searchName"), Updates.unset("searchDescription")),
        )

        BackfillSearchFieldsChangeUnit().execute(mongoTemplate)

        for (query in listOf("lodz", "cafe")) {
            mockMvc
                .get("/api/materials") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(1) }
                }
        }
    }

    @Test
    fun `should refresh material search fields after an edit`() {
        val userId = UUID.randomUUID()
        val material = materialRepository.save(createTestMaterial(userId = userId, name = "Łódź", description = ""))
        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateMaterialJson(name = "Café", description = "Düsseldorf")
            }.andExpect {
                status { isOk() }
            }

        for ((query, expected) in listOf("lodz" to 0, "cafe" to 1, "dusseldorf" to 1)) {
            mockMvc
                .get("/api/materials") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(expected) }
                }
        }
    }

    @Test
    fun `should handle special characters in search query`() {
        val userId = UUID.randomUUID()
        materialRepository.save(createTestMaterial(userId, name = "Material (Special)"))
        materialRepository.save(createTestMaterial(userId, name = "Material ."))

        mockMvc
            .get("/api/materials?query=(Special)") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].name") { value("Material (Special)") }
            }

        mockMvc
            .get("/api/materials?query=.") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].name") { value("Material .") }
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
