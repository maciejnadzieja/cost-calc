package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.Service
import com.costcalc.api.model.UserId
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.repository.ServiceRepository
import java.math.BigDecimal
import java.util.UUID
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

class ServiceRestControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @BeforeEach
    fun setUp() {
        serviceRepository.deleteAll()
    }

    @Test
    fun `should return all units`() {
        mockMvc
            .get("/api/services/units") {
                with(jwtRequestPostProcessor())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should handle service lifecycle`() {
        val userId = UUID.randomUUID()

        // Create
        val result =
            mockMvc
                .post("/api/services") {
                    with(jwtRequestPostProcessor(userId))
                    contentType = MediaType.APPLICATION_JSON
                    content = createServiceJson()
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { exists() }
                }.andReturn()

        val createdServiceId =
            com.fasterxml.jackson.databind
                .ObjectMapper()
                .readTree(result.response.contentAsString)
                .get("id")
                .asText()

        // Get by id
        mockMvc
            .get("/api/services/{id}", createdServiceId) {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Service") }
            }

        // Search
        mockMvc
            .get("/api/services") {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Test Service") }
            }

        // Delete
        mockMvc
            .delete("/api/services/{id}", createdServiceId) {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
            }

        // Verify deletion
        mockMvc
            .get("/api/services/{id}", createdServiceId) {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should not allow user to access services of another user`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val service = createTestService(userId = userId1)
        serviceRepository.save(service)

        // User 1 can access their own service
        mockMvc
            .get("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId1))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Service") }
            }

        // User 2 cannot access User 1's service
        mockMvc
            .get("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should allow user to update their own service`() {
        val userId = UUID.randomUUID()
        val service = createTestService(userId = userId)
        serviceRepository.save(service)

        mockMvc
            .put("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateServiceJson()
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Updated Service") }
            }
    }

    @Test
    fun `should not allow user to update another user's service`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val service = createTestService(userId = userId1)
        serviceRepository.save(service)

        mockMvc
            .put("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId2))
                contentType = MediaType.APPLICATION_JSON
                content = updateServiceJson()
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input on update`() {
        val userId = UUID.randomUUID()
        val service = createTestService(userId = userId)
        serviceRepository.save(service)

        mockMvc
            .put("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateServiceJson(price = -20.0)
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should allow user to delete their own service`() {
        val userId = UUID.randomUUID()
        val service = createTestService(userId = userId)
        serviceRepository.save(service)

        mockMvc
            .delete("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should not allow user to delete another user's service`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val service = createTestService(userId = userId1)
        serviceRepository.save(service)

        mockMvc
            .delete("/api/services/{id}", service.id) {
                with(jwtRequestPostProcessor(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input price`() {
        mockMvc
            .post("/api/services") {
                with(jwtRequestPostProcessor())
                contentType = MediaType.APPLICATION_JSON
                content = createServiceJson(price = -10.0)
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return bad request for invalid input name`() {
        mockMvc
            .post("/api/services") {
                with(jwtRequestPostProcessor())
                contentType = MediaType.APPLICATION_JSON
                content = createServiceJson(name = "")
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should secure all routes`() {
        mockMvc.get("/api/services").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/services/units").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/services/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
        mockMvc
            .post("/api/services") {
                contentType = MediaType.APPLICATION_JSON
                content = createServiceJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc
            .put("/api/services/{id}", UUID.randomUUID()) {
                contentType = MediaType.APPLICATION_JSON
                content = updateServiceJson()
            }.andExpect { status { isUnauthorized() } }
        mockMvc.delete("/api/services/{id}", UUID.randomUUID()).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `should return paged results`() {
        val userId = UUID.randomUUID()
        (1..15).forEach {
            serviceRepository.save(createTestService(userId = userId, name = "Service $it"))
        }

        // First page
        mockMvc
            .get("/api/services?page=0&size=10") {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(10) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }

        // Second page
        mockMvc
            .get("/api/services?page=1&size=10") {
                with(jwtRequestPostProcessor(userId))
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
            .get("/api/services?page=-1&size=10") {
                with(jwtRequestPostProcessor())
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/services?page=0&size=-10") {
                with(jwtRequestPostProcessor())
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should filter services by name`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "Test Service 1"))
        serviceRepository.save(createTestService(userId = userId, name = "Test Service 2"))
        serviceRepository.save(createTestService(userId = userId, name = "Another Service"))

        mockMvc
            .get("/api/services?query=Test") {
                with(jwtRequestPostProcessor(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
            }
    }

    private fun jwtRequestPostProcessor(
        userId: UserId = UUID.randomUUID(),
        email: String = "email@example.com",
    ) = jwt().jwt {
        it.claim("sub", userId.toString())
        it.claim("email", email)
    }

    private fun createTestService(
        userId: UserId,
        id: UUID = UUID.randomUUID(),
        name: String = "Test Service",
        description: String = "$name Description",
        price: BigDecimal = BigDecimal.TEN,
        workUnit: WorkUnit = WorkUnit.HOUR,
    ) = Service(
        id = id,
        userId = userId,
        name = name,
        description = description,
        price = price,
        workUnit = workUnit,
    )

    private fun createServiceJson(
        name: String = "Test Service",
        description: String = "$name Description",
        price: Double = 10.0,
        workUnit: WorkUnit = WorkUnit.HOUR,
    ) = """
        {
            "name": "$name",
            "description": "$description",
            "price": $price,
            "workUnit": "$workUnit"
        }
        """.trimIndent()

    private fun updateServiceJson(
        name: String = "Updated Service",
        description: String = "Updated Description",
        price: Double = 20.0,
        workUnit: WorkUnit = WorkUnit.DAY,
    ) = """
        {
            "name": "$name",
            "description": "$description",
            "price": $price,
            "workUnit": "$workUnit"
        }
        """.trimIndent()
}
