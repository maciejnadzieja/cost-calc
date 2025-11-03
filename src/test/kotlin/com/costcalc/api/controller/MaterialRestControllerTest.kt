
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
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
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
    fun `should return all units`() {
        mockMvc
            .get("/api/materials/units") {
                with(jwt(UUID.randomUUID(), "email@example.com"))
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
                    with(jwt(userId, "email@example.com"))
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                        {
                            "name": "Test Material",
                            "description": "Test Description",
                            "price": 10.0,
                            "unit": "KILOGRAM",
                            "category": "Test Category"
                        }
                        """.trimIndent()
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
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Material") }
            }

        // Search
        mockMvc
            .get("/api/materials") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Test Material") }
            }

        // Delete
        mockMvc
            .delete("/api/materials/{id}", createdMaterialId) {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
            }

        // Verify deletion
        mockMvc
            .get("/api/materials/{id}", createdMaterialId) {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isNotFound() }
            }
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

    @Test
    fun `should allow user to update their own material`() {
        val userId = UUID.randomUUID()
        val material =
            Material.create(
                name = "Test Material",
                description = "Test Description",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Test Category",
                userId = userId,
            )
        materialRepository.save(material)

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(jwt(userId, "email@example.com"))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Updated Material",
                        "description": "Updated Description",
                        "price": 20.0,
                        "unit": "GRAM",
                        "category": "Updated Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Updated Material") }
            }
    }

    @Test
    fun `should not allow user to update another user's material`() {
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

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(jwt(userId2, "email2@example.com"))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Updated Material",
                        "description": "Updated Description",
                        "price": 20.0,
                        "unit": "GRAM",
                        "category": "Updated Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input on update`() {
        val userId = UUID.randomUUID()
        val material =
            Material.create(
                name = "Test Material",
                description = "Test Description",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Test Category",
                userId = userId,
            )
        materialRepository.save(material)

        mockMvc
            .put("/api/materials/{id}", material.id) {
                with(jwt(userId, "email@example.com"))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Updated Material",
                        "description": "Updated Description",
                        "price": -20.0,
                        "unit": "GRAM",
                        "category": "Updated Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should allow user to delete their own material`() {
        val userId = UUID.randomUUID()
        val material =
            Material.create(
                name = "Test Material",
                description = "Test Description",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Test Category",
                userId = userId,
            )
        materialRepository.save(material)

        mockMvc
            .delete("/api/materials/{id}", material.id) {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should not allow user to delete another user's material`() {
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

        mockMvc
            .delete("/api/materials/{id}", material.id) {
                with(jwt(userId2, "email2@example.com"))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input price`() {
        val userId = UUID.randomUUID()
        mockMvc
            .post("/api/materials") {
                with(jwt(userId, "email@example.com"))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Test name",
                        "description": "Test Description",
                        "price": -10.0,
                        "unit": "KILOGRAM",
                        "category": "Test Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return bad request for invalid input name`() {
        val userId = UUID.randomUUID()
        mockMvc
            .post("/api/materials") {
                with(jwt(userId, "email@example.com"))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "",
                        "description": "Test Description",
                        "price": 10.0,
                        "unit": "KILOGRAM",
                        "category": "Test Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should secure all routes`() {
        mockMvc
            .get("/api/materials")
            .andExpect {
                status { isUnauthorized() }
            }
        mockMvc
            .get("/api/materials/units")
            .andExpect {
                status { isUnauthorized() }
            }
        mockMvc
            .get("/api/materials/{id}", UUID.randomUUID())
            .andExpect {
                status { isUnauthorized() }
            }
        mockMvc
            .post("/api/materials") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Test Material",
                        "description": "Test Description",
                        "price": 10.0,
                        "unit": "KILOGRAM",
                        "category": "Test Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isUnauthorized() }
            }
        mockMvc
            .put("/api/materials/{id}", UUID.randomUUID()) {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "Updated Material",
                        "description": "Updated Description",
                        "price": 20.0,
                        "unit": "GRAM",
                        "category": "Updated Category"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isUnauthorized() }
            }
        mockMvc
            .delete("/api/materials/{id}", UUID.randomUUID())
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `should return paged results`() {
        val userId = UUID.randomUUID()
        (1..15).forEach {
            materialRepository.save(
                Material.create(
                    name = "Material $it",
                    description = "Description $it",
                    price = BigDecimal.valueOf(it.toLong()),
                    unit = MaterialUnit.KILOGRAM,
                    category = "Category",
                    userId = userId,
                ),
            )
        }

        // First page
        mockMvc
            .get("/api/materials?page=0&size=10") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(10) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }

        // Second page
        mockMvc
            .get("/api/materials?page=1&size=10") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(5) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }
    }

    @Test
    fun `should return bad request for invalid paging params`() {
        val userId = UUID.randomUUID()

        mockMvc
            .get("/api/materials?page=-1&size=10") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/materials?page=0&size=-10") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should filter materials by name`() {
        val userId = UUID.randomUUID()
        materialRepository.save(
            Material.create(
                name = "Test Material 1",
                description = "Description 1",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Category",
                userId = userId,
            ),
        )
        materialRepository.save(
            Material.create(
                name = "Test Material 2",
                description = "Description 2",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Category",
                userId = userId,
            ),
        )
        materialRepository.save(
            Material.create(
                name = "Another Material",
                description = "Description 3",
                price = BigDecimal.TEN,
                unit = MaterialUnit.KILOGRAM,
                category = "Category",
                userId = userId,
            ),
        )

        mockMvc
            .get("/api/materials?query=Test") {
                with(jwt(userId, "email@example.com"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
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
