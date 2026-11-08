package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.model.Service
import com.costcalc.api.model.UserId
import com.costcalc.api.model.WorkUnit
import com.costcalc.api.repository.ServiceRepository
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

class ServiceRestControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setUp() {
        serviceRepository.deleteAll()
    }

    @Test
    fun `should return all units`() {
        mockMvc
            .get("/api/services/units") {
                with(token())
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
                    with(token(userId))
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
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Service") }
            }

        // Search
        mockMvc
            .get("/api/services") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Test Service") }
            }

        // Delete
        mockMvc
            .delete("/api/services/{id}", createdServiceId) {
                with(token(userId))
            }.andExpect {
                status { isOk() }
            }

        // Verify deletion
        mockMvc
            .get("/api/services/{id}", createdServiceId) {
                with(token(userId))
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
                with(token(userId1))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Test Service") }
            }

        // User 2 cannot access User 1's service
        mockMvc
            .get("/api/services/{id}", service.id) {
                with(token(userId2))
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
                with(token(userId))
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
                with(token(userId2))
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
                with(token(userId))
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
                with(token(userId))
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
                with(token(userId2))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bad request for invalid input price`() {
        mockMvc
            .post("/api/services") {
                with(token())
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
                with(token())
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
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(10) }
                jsonPath("$.page.totalPages") { value(2) }
                jsonPath("$.page.totalElements") { value(15) }
            }

        // Second page
        mockMvc
            .get("/api/services?page=1&size=10") {
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
            .get("/api/services?page=-1&size=10") {
                with(token())
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/services?page=0&size=-10") {
                with(token())
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
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
            }
    }

    @Test
    fun `should find a service by a fragment inside its name or description`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "Instalacja", description = ""))
        serviceRepository.save(createTestService(userId = userId, name = "Other", description = "Montaż instalacji"))

        mockMvc
            .get("/api/services?query=stal") {
                with(token(userId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.page.totalElements") { value(2) }
            }
    }

    @Test
    fun `should find services with or without Polish accents in either direction`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "Łódź (a+b)", description = ""))
        serviceRepository.save(createTestService(userId = userId, name = "Lodz", description = ""))

        for (query in listOf("lodz", "Łódź")) {
            mockMvc
                .get("/api/services") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(2) }
                    jsonPath("$.page.totalElements") { value(2) }
                }
        }

        mockMvc
            .get("/api/services") {
                with(token(userId))
                param("query", "lodz (a+b)")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].name") { value("Łódź (a+b)") }
            }
    }

    @Test
    fun `should match every Polish letter in paged user scoped service search`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "ZAŻÓŁĆ GĘŚLĄ JAŹŃ", description = ""))
        serviceRepository.save(createTestService(userId = userId, name = "Other", description = "Zazolc gesla jazn"))
        serviceRepository.save(createTestService(userId = UUID.randomUUID(), name = "Zażółć gęślą jaźń", description = ""))

        for (query in listOf("zazolc gesla jazn", "ZAŻÓŁĆ GĘŚLĄ JAŹŃ")) {
            for (page in 0..1) {
                mockMvc
                    .get("/api/services") {
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
    fun `should match accents from other languages in service names`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "Café Düsseldorf", description = ""))

        mockMvc
            .get("/api/services") {
                with(token(userId))
                param("query", "CAFE DUSSELDORF")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("Café Düsseldorf") }
            }
    }

    @Test
    fun `should backfill search fields for an existing service`() {
        val userId = UUID.randomUUID()
        serviceRepository.save(createTestService(userId = userId, name = "Łódź", description = "Café"))
        mongoTemplate.db.getCollection("services").updateOne(
            Filters.eq("name", "Łódź"),
            Updates.combine(Updates.unset("searchName"), Updates.unset("searchDescription")),
        )

        BackfillSearchFieldsChangeUnit().execute(mongoTemplate)

        for (query in listOf("lodz", "cafe")) {
            mockMvc
                .get("/api/services") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(1) }
                }
        }
    }

    @Test
    fun `should refresh service search fields after an edit`() {
        val userId = UUID.randomUUID()
        val service = serviceRepository.save(createTestService(userId = userId, name = "Łódź", description = ""))
        mockMvc
            .put("/api/services/{id}", service.id) {
                with(token(userId))
                contentType = MediaType.APPLICATION_JSON
                content = updateServiceJson(name = "Café", description = "Düsseldorf")
            }.andExpect {
                status { isOk() }
            }

        for ((query, expected) in listOf("lodz" to 0, "cafe" to 1, "dusseldorf" to 1)) {
            mockMvc
                .get("/api/services") {
                    with(token(userId))
                    param("query", query)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content.length()") { value(expected) }
                }
        }
    }

    private fun token(
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
