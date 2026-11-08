package com.costcalc.api.search

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID

class ExternalMaterialSearchTest {
    @Test
    fun `discovery limits provider results to eligible pages on selected sources`() {
        val http = ExternalMaterialHttp { uri, headers, _ ->
            assertEquals("api.search.brave.com", uri.host)
            assertEquals("test-key", headers["X-Subscription-Token"])
            assertEquals(true, uri.query.contains("site:catalog.example"))
            ExternalHttpResponse(
                200,
                "application/json",
                null,
                """
                {"web":{"results":[
                  {"title":"<strong>Tawuła</strong>","url":"https://catalog.example/plants/12,name","description":"Plant"},
                  {"title":"Index","url":"https://catalog.example/plants/"},
                  {"title":"Redirect trick","url":"https://catalog.example.evil.test/plants/1,name"},
                  {"title":"Other source","url":"https://atlas.example/species/Alba.htm"}
                ]}}
                """.trimIndent().toByteArray(),
            )
        }
        val search = ExternalMaterialSearch(properties(), http, ObjectMapper())

        val results = search.search(UUID.randomUUID(), "plants", "tawuła", listOf("catalog"))

        assertEquals(listOf("Tawuła"), results.map { it.title })
        assertEquals("https://catalog.example/plants/12,name", results.single().sourceUrl)
    }

    @Test
    fun `only public HTTPS item paths belong to a source`() {
        val source = properties().sources.getValue("catalog")
        assertEquals(true, source.allowsPage(URI("https://www.catalog.example/plants/12%2Cname")))
        for (url in listOf(
            "http://catalog.example/plants/12,name",
            "https://catalog.example:444/plants/12,name",
            "https://catalog.example/plants/12,name/../login",
            "https://catalog.example/plants%2F12,name",
            "https://catalog.example@elsewhere.example/plants/12,name",
        )) {
            assertEquals(false, source.allowsPage(URI(url)), url)
        }
    }

    private fun properties() = ExternalMaterialSearchProperties(
        braveApiKey = "test-key",
        categories = mapOf("plants" to listOf("catalog", "atlas")),
        sources = mapOf(
            "catalog" to ExternalMaterialSource("Catalog", "catalog.example", "/plants/[0-9]+,[a-z]+"),
            "atlas" to ExternalMaterialSource("Atlas", "atlas.example", "/species/[A-Za-z]+[.]htm"),
        ),
    )
}
