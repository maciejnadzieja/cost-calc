package com.costcalc.api.search

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.URI
import java.time.Duration
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

    @Test
    fun draftRejectsOtherUsersAndRedirectsOutsideTheSource() {
        val calls = mutableListOf<URI>()
        val search = ExternalMaterialSearch(properties(), ExternalMaterialHttp { uri, headers, _ ->
            calls += uri
            if (calls.size == 1) {
                ExternalHttpResponse(200, "application/json", null,
                    """{"web":{"results":[{"title":"Plant","url":"https://catalog.example/plants/12,name"}]}}""".toByteArray())
            } else {
                assertEquals(null, headers["X-Subscription-Token"])
                ExternalHttpResponse(302, null, "https://atlas.example/species/Alba.htm", byteArrayOf())
            }
        }, ObjectMapper())
        val owner = UUID.randomUUID()
        val id = search.search(owner, "plants", "plant", null).single().id

        assertEquals(ExternalMaterialSearchError.RESULT_EXPIRED, assertThrows(ExternalMaterialSearchException::class.java) {
            search.draft(UUID.randomUUID(), "plants", id)
        }.error)
        assertEquals(1, calls.size)
        assertEquals(ExternalMaterialSearchError.UNSUPPORTED_PAGE, assertThrows(ExternalMaterialSearchException::class.java) {
            search.draft(owner, "plants", id)
        }.error)
        assertEquals(2, calls.size)
        assertEquals(ExternalMaterialSearchError.SEARCH_BUSY, assertThrows(ExternalMaterialSearchException::class.java) {
            search.search(owner, "plants", "plant", null)
        }.error)
    }

    @Test
    fun rejectsPrivateAndSpecialDestinationAddresses() {
        for (ip in listOf("127.0.0.1", "100.64.0.1", "169.254.169.254", "192.168.1.1", "::1", "fd00::1", "2002:7f00:1::")) {
            assertEquals(false, PublicExternalDnsResolver.isPublic(InetAddress.getByName(ip)), ip)
        }
        assertEquals(true, PublicExternalDnsResolver.isPublic(InetAddress.getByName("8.8.8.8")))
    }

    @Test
    fun resultEntriesExpireAndStayBounded() {
        var time = 0L
        val entries = BoundedMaterialEntries<String>(Duration.ofNanos(100), 1) { time }
        val owner = UUID.randomUUID()
        val first = entries.issue(owner, "old")
        val second = entries.issue(owner, "current")
        assertEquals(null, entries.find(owner, first))
        assertEquals("current", entries.find(owner, second))
        time = 100
        assertEquals(null, entries.find(owner, second))
    }
}
