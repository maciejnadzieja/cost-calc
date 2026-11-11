package com.costcalc.api.search

import com.costcalc.api.model.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import org.jsoup.Jsoup
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.URI
import java.time.Duration
import java.util.UUID

data class ExternalMaterialSourceOption(val id: String, val label: String)

data class ExternalMaterialResult(
    val id: UUID,
    val title: String,
    val excerpt: String,
    val sourceLabel: String,
    val sourceUrl: String,
)

data class ExternalMaterialDetails(
    val name: String,
    val description: String,
    val sourceUrl: String,
    val imageUrl: String?,
)

@Service
@EnableConfigurationProperties(ExternalMaterialSearchProperties::class)
@PropertySource("classpath:external-material-search.properties")
class ExternalMaterialSearch(
    private val properties: ExternalMaterialSearchProperties,
    private val http: ExternalMaterialHttp,
    private val mapper: ObjectMapper,
) {
    private data class IssuedResult(val category: String, val sourceId: String, val url: URI)

    private val results = BoundedMaterialEntries<IssuedResult>(Duration.ofMinutes(15), 1_000)
    private var lastSearch: Long? = null

    fun sources(category: String): List<ExternalMaterialSourceOption> =
        enabledSources(category).map { (id, source) -> ExternalMaterialSourceOption(id, source.label) }

    fun search(userId: UserId, category: String, query: String, sourceIds: List<String>?): List<ExternalMaterialResult> {
        val enabled = enabledSources(category)
        val selected = sourceIds?.let {
            if (it.isEmpty() || it.distinct().size != it.size || !enabled.keys.containsAll(it)) {
                fail(ExternalMaterialSearchError.INVALID_SEARCH)
            }
            enabled.filterKeys(it::contains)
        } ?: enabled
        val term = query.trim()
        if (term.isBlank() || term.length > 120) fail(ExternalMaterialSearchError.INVALID_SEARCH)
        if (properties.braveApiKey.isBlank() || selected.isEmpty()) fail(ExternalMaterialSearchError.SEARCH_UNAVAILABLE)
        reserveSearch()

        val sites = selected.values.joinToString(" OR ") { "site:" + it.domain }
        val uri = UriComponentsBuilder.fromUriString("https://api.search.brave.com/res/v1/web/search")
            .queryParam("q", "{query}").queryParam("country", "PL")
            .queryParam("search_lang", "pl").queryParam("count", 10)
            .encode().buildAndExpand("$term ($sites)").toUri()
        val response = try {
            http.get(
                uri,
                mapOf("Accept" to "application/json", "X-Subscription-Token" to properties.braveApiKey),
                256_000,
            )
        } catch (_: IOException) {
            fail(ExternalMaterialSearchError.SEARCH_UNAVAILABLE)
        }
        if (response.status == 429) fail(ExternalMaterialSearchError.SEARCH_BUSY)
        if (response.status != 200) fail(ExternalMaterialSearchError.SEARCH_UNAVAILABLE)
        val json = try {
            mapper.readTree(response.body)
        } catch (_: IOException) {
            fail(ExternalMaterialSearchError.SEARCH_UNAVAILABLE)
        }
        if (json == null || !json.isObject) fail(ExternalMaterialSearchError.SEARCH_UNAVAILABLE)

        val seen = mutableSetOf<URI>()
        return json.path("web").path("results").asSequence().take(20).mapNotNull { item ->
            val url = parseUri(item.path("url").asText()) ?: return@mapNotNull null
            val (sourceId, source) = selected.entries.firstOrNull { it.value.allowsPage(url) } ?: return@mapNotNull null
            val title = plainSnippet(item.path("title").asText(), 250)
            if (title.isBlank() || !seen.add(url)) return@mapNotNull null
            val id = results.issue(userId, IssuedResult(category, sourceId, url))
            ExternalMaterialResult(id, title, plainSnippet(item.path("description").asText(), 500), source.label, url.toASCIIString())
        }.take(10).toList()
    }

    fun details(userId: UserId, category: String, resultId: UUID): ExternalMaterialDetails {
        val page = fetchPage(userId, category, resultId)
        val details = MaterialHtmlExtractor.extract(page)
        return ExternalMaterialDetails(details.name, details.description, page.url.toASCIIString(), details.imageUrl)
    }

    private fun fetchPage(userId: UserId, category: String, resultId: UUID): FetchedMaterialPage {
        val result = results.find(userId, resultId)?.takeIf { it.category == category }
            ?: fail(ExternalMaterialSearchError.RESULT_EXPIRED)
        val source = enabledSources(category)[result.sourceId] ?: fail(ExternalMaterialSearchError.RESULT_EXPIRED)
        val url = result.url
        if (!source.allowsPage(url)) fail(ExternalMaterialSearchError.UNSUPPORTED_PAGE)
        val response = try {
            http.get(url, mapOf("Accept" to "text/html", "User-Agent" to "CostCalcMaterialSearch/1.0"), 1_500_000)
        } catch (_: IOException) {
            fail(ExternalMaterialSearchError.SOURCE_UNAVAILABLE)
        }
        if (response.status != 200) fail(ExternalMaterialSearchError.SOURCE_UNAVAILABLE)
        val contentType = response.contentType.orEmpty()
        if (!contentType.substringBefore(';').trim().equals("text/html", ignoreCase = true)) {
            fail(ExternalMaterialSearchError.UNSUPPORTED_PAGE)
        }
        return FetchedMaterialPage(url, source, contentType, response.body)
    }

    private fun enabledSources(category: String): Map<String, ExternalMaterialSource> =
        (properties.categories[category] ?: fail(ExternalMaterialSearchError.UNKNOWN_CATEGORY))
            .associateWith(properties.sources::getValue).filterValues { it.enabled }

    @Synchronized
    private fun reserveSearch() {
        val now = System.nanoTime()
        if (lastSearch?.let { now - it < properties.minRequestInterval.toNanos() } == true) {
            fail(ExternalMaterialSearchError.SEARCH_BUSY)
        }
        lastSearch = now
    }

    private fun plainSnippet(value: String, limit: Int): String =
        Jsoup.parseBodyFragment(value.take(5_000)).text().take(limit)
}

internal data class FetchedMaterialPage(
    val url: URI,
    val source: ExternalMaterialSource,
    val contentType: String,
    val body: ByteArray,
)

internal fun parseUri(value: String): URI? = if (value.length in 1..2048) {
    runCatching { URI(value) }.getOrNull()
} else {
    null
}

internal fun fail(error: ExternalMaterialSearchError): Nothing = throw ExternalMaterialSearchException(error)
