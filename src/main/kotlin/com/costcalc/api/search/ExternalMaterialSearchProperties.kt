package com.costcalc.api.search

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration
import java.util.Locale

@ConfigurationProperties("costcalc.external-material-search")
class ExternalMaterialSearchProperties(
    val braveApiKey: String = "",
    val minRequestInterval: Duration = Duration.ofSeconds(1),
    val categories: Map<String, List<String>> = emptyMap(),
    val sources: Map<String, ExternalMaterialSource> = emptyMap(),
) {
    init {
        val id = Regex("[a-z][a-z0-9-]{0,39}")
        require(minRequestInterval >= Duration.ofSeconds(1) && minRequestInterval <= Duration.ofMinutes(1)) {
            "External material search interval must be between one second and one minute"
        }
        require(sources.keys.all(id::matches) && categories.keys.all(id::matches)) {
            "External material search configuration contains an invalid ID"
        }
        require(categories.values.all { it.isNotEmpty() && it.size <= 10 && it.distinct().size == it.size && sources.keys.containsAll(it) }) {
            "External material search categories must refer to distinct configured sources"
        }
    }
}

data class ExternalMaterialSource(
    val label: String,
    val domain: String,
    val itemPath: String,
    val enabled: Boolean = true,
) {
    private val eligiblePath = Regex(itemPath)

    init {
        require(label.isNotBlank() && label.length <= 100)
        require(domain.matches(Regex("[a-z0-9-]+(?:[.][a-z0-9-]+)*[.][a-z]{2,63}")) && domain.length <= 253)
        require(itemPath.startsWith("/") && itemPath.length <= 256 && !eligiblePath.matches("/"))
    }

    fun allowsPage(uri: URI): Boolean =
        enabled && allowsOrigin(uri) && uri.rawQuery == null &&
            uri.rawPath == uri.normalize().rawPath &&
            !Regex("%(?:2f|5c|25)", RegexOption.IGNORE_CASE).containsMatchIn(uri.rawPath.orEmpty()) &&
            uri.path.orEmpty().split('/').none { it == "." || it == ".." } &&
            eligiblePath.matches(uri.path.orEmpty())

    fun allowsOrigin(uri: URI): Boolean =
        uri.scheme.equals("https", ignoreCase = true) &&
            uri.host?.lowercase(Locale.ROOT) in setOf(domain, "www.$domain") &&
            uri.port in setOf(-1, 443) && uri.userInfo == null && uri.fragment == null &&
            uri.toASCIIString().length <= 2048
}
