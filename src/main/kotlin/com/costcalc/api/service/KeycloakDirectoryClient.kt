package com.costcalc.api.service

import com.costcalc.api.config.KeycloakDirectoryProperties
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.StreamingHttpOutputMessage
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DirectoryUnavailableException : RuntimeException("User directory is unavailable")

@Component
class KeycloakDirectoryClient(
    private val properties: KeycloakDirectoryProperties,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") issuer: String,
    @Qualifier("directoryRestClient") private val client: RestClient,
) {
    private val issuerUri = URI.create(issuer.trimEnd('/'))
    private val realm = issuerUri.path.substringAfterLast("/realms/", "")
    private val server = properties.serverUrl.ifBlank {
        issuerUri.toString().substringBeforeLast("/realms/")
    }.trimEnd('/')

    fun token(): String = safely {
        if (realm.isBlank() || realm.contains('/') || properties.clientId.isBlank() || properties.clientSecret.isBlank()) {
            throw DirectoryUnavailableException()
        }
        val form = listOf(
            "grant_type" to "client_credentials",
            "client_id" to properties.clientId,
            "client_secret" to properties.clientSecret,
        ).joinToString("&") { (key, value) -> "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}" }
        // The converter-based body(Object) API logs form values at DEBUG, which this app enables.
        // Stream the encoded form so credentials never enter that request-body log path.
        val response = client.post().uri(url("realms", realm, "protocol", "openid-connect", "token"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(StreamingHttpOutputMessage.Body { it.write(form.toByteArray(StandardCharsets.UTF_8)) })
            .retrieve().body(JsonNode::class.java)
        response?.get("access_token")?.takeIf { it.isTextual && it.asText().isNotBlank() }?.asText()
            ?: throw DirectoryUnavailableException()
    }

    fun users(token: String, first: Int, max: Int): JsonNode = safely {
        val uri = UriComponentsBuilder.fromUri(url("admin", "realms", realm, "users"))
            .queryParam("first", first).queryParam("max", max)
            // Full representation includes the service-account link needed for defensive filtering.
            .queryParam("briefRepresentation", false).build().toUri()
        val response = client.get().uri(uri).headers { it.setBearerAuth(token) }
            .retrieve().body(JsonNode::class.java)
        response?.takeIf { it.isArray && it.size() <= max } ?: throw DirectoryUnavailableException()
    }

    private fun url(vararg paths: String): URI =
        UriComponentsBuilder.fromUriString(server).pathSegment(*paths).build().encode().toUri()

    private fun <T> safely(operation: () -> T): T = try {
        operation()
    } catch (_: RestClientException) {
        // Discard raw upstream bodies, tokens, URLs and exception causes.
        throw DirectoryUnavailableException()
    } catch (_: IllegalArgumentException) {
        throw DirectoryUnavailableException()
    }
}
