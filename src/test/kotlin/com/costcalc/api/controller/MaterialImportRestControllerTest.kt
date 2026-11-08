package com.costcalc.api.controller

import com.costcalc.api.AbstractIntegrationTest
import com.costcalc.api.repository.MaterialRepository
import com.costcalc.api.search.ExternalHttpResponse
import com.costcalc.api.search.ExternalMaterialHttp
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.net.URI
import java.util.UUID

@TestPropertySource(properties = ["costcalc.external-material-search.brave-api-key=fixture"])
class MaterialImportRestControllerTest : AbstractIntegrationTest() {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var mapper: ObjectMapper
    @Autowired private lateinit var materials: MaterialRepository
    @MockitoBean private lateinit var http: ExternalMaterialHttp

    @BeforeEach
    fun setUp() {
        materials.deleteAll()
    }

    @Test
    fun createValidatesLinksWithoutDependingOnSearchState() {
        whenever(http.get(any(), any(), any())).thenAnswer { call ->
            when (call.getArgument<URI>(0).host) {
                "api.search.brave.com" -> response("application/json", """
                    {"web":{"results":[
                      {"title":"Spiraea alba","url":"https://e-katalogroslin.pl/plants/12,tawula"},
                      {"title":"Atlas plant","url":"https://atlas-roslin.pl/gatunki/Spiraea_alba.htm"}
                    ]}}
                """)
                "e-katalogroslin.pl" -> response("text/html", """
                    <main><h1>Spiraea alba</h1><img alt="Spiraea alba" src="/photo.jpg">
                    <dl><dt>Colour</dt><dd>White</dd></dl></main>
                """)
                "atlas-roslin.pl" -> response("text/html", "<title>Atlas plant</title><main><p>Public description</p></main>")
                else -> error("Unexpected test request")
            }
        }
        val owner = UUID.randomUUID()
        val other = UUID.randomUUID()
        mvc.get("$searchPath/sources").andExpect { status { isUnauthorized() } }
        mvc.get("$searchPath/sources") { with(token(owner)) }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("ekatalog") }
            jsonPath("$[1].id") { value("atlas") }
        }
        val legacyId = parse(mvc.post("/api/materials") {
            with(token(owner))
            contentType = MediaType.APPLICATION_JSON
            content = payload("Manual", "Previously saved")
        }.andExpect { status { isOk() } }.andReturn().response.contentAsByteArray)["id"].asText()
        val results = parse(mvc.post("$searchPath/results") {
            with(token(owner))
            contentType = MediaType.APPLICATION_JSON
            content = """{"query":"tawuła"}"""
        }.andExpect { status { isOk() } }.andReturn().response.contentAsByteArray)
        val detailsPath = "$searchPath/results/" + results[0]["id"].asText() + "/details"
        mvc.post(detailsPath) { with(token(other)) }.andExpect { status { isNotFound() } }
        val details = parse(mvc.post(detailsPath) { with(token(owner)) }.andExpect {
            status { isOk() }
            jsonPath("$.description") { value("Colour: White") }
            jsonPath("$.id") { doesNotExist() }
        }.andReturn().response.contentAsByteArray)
        val source = details["sourceUrl"].asText()
        val image = details["imageUrl"].asText()

        val description = "x".repeat(498) + "\na"
        create(owner, payload("Imported", description, "https://untrusted.example/page", image))
            .andExpect { status { isBadRequest() } }
        create(owner, payload("Imported", description, source, "https://atlas-roslin.pl/photo.jpg"))
            .andExpect { status { isBadRequest() } }
        create(owner, payload("Imported", description, source, "https://www.e-katalogroslin.pl/photo.jpg"))
            .andExpect { status { isBadRequest() } }
        create(owner, payload("Imported", description, imageUrl = image)).andExpect { status { isBadRequest() } }
        create(owner, payload("Imported", description + "b", source, image)).andExpect { status { isBadRequest() } }
        create(owner, payload("Imported", description, source, image, price = 0)).andExpect { status { isBadRequest() } }
        mvc.get("/api/materials") { with(token(owner)) }.andExpect { jsonPath("$.page.totalElements") { value(1) } }

        val created = create(owner, payload("Imported", description, source, image)).andExpect {
            status { isOk() }
            jsonPath("$.sourceUrl") { value("https://e-katalogroslin.pl/plants/12,tawula") }
            jsonPath("$.imageUrl") { value("https://e-katalogroslin.pl/photo.jpg") }
            jsonPath("$.description") { value(description) }
        }.andReturn().response.contentAsByteArray
        val id = parse(created)["id"].asText()
        mvc.get("/api/materials/$id") { with(token(other)) }.andExpect { status { isNotFound() } }

        mvc.put("/api/materials/$id") {
            with(token(owner))
            contentType = MediaType.APPLICATION_JSON
            content = payload("Edited normally", description, "https://untrusted.example/page", null)
        }.andExpect { status { isOk() } }
        mvc.get("/api/materials/$id") { with(token(owner)) }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Edited normally") }
            jsonPath("$.sourceUrl") { value("https://e-katalogroslin.pl/plants/12,tawula") }
            jsonPath("$.imageUrl") { value("https://e-katalogroslin.pl/photo.jpg") }
            jsonPath("$.description") { value(description) }
        }
        mvc.get("/api/materials/$legacyId") { with(token(owner)) }.andExpect {
            jsonPath("$.description") { value("Previously saved") }
            jsonPath("$.sourceUrl") { doesNotExist() }
        }
        // Saving a valid source link does not require first calling search or details.
        create(owner, payload("Atlas plant", "Public description", "https://atlas-roslin.pl/gatunki/Spiraea_alba.htm")).andExpect {
            status { isOk() }
            jsonPath("$.imageUrl") { doesNotExist() }
        }
        mvc.get("/api/materials") { with(token(owner)) }.andExpect { jsonPath("$.page.totalElements") { value(3) } }
    }

    private fun create(owner: UUID, body: String) = mvc.post("/api/materials") {
        with(token(owner))
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    private fun payload(
        name: String,
        description: String,
        sourceUrl: String? = null,
        imageUrl: String? = null,
        price: Int = 12,
    ) =
        mapper.writeValueAsString(mapOf(
            "name" to name, "description" to description, "price" to price, "unit" to "PIECE",
            "category" to "Plants", "sourceUrl" to sourceUrl, "imageUrl" to imageUrl,
        ))

    private fun token(owner: UUID) = jwt().jwt {
        it.subject(owner.toString()).claim("email", "$owner@example.com")
    }

    private fun parse(bytes: ByteArray) = mapper.readTree(bytes)

    private fun response(type: String, body: String) = ExternalHttpResponse(200, type, null, body.toByteArray())

    private val searchPath = "/api/materials/external-search/plants"
}
