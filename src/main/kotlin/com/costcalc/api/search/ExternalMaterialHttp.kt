package com.costcalc.api.search

import jakarta.annotation.PreDestroy
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.NoOpResponseErrorHandler
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.zip.GZIPInputStream

fun interface ExternalMaterialHttp {
    @Throws(IOException::class)
    fun get(uri: URI, headers: Map<String, String>, maxBytes: Int): ExternalHttpResponse
}

data class ExternalHttpResponse(
    val status: Int,
    val contentType: String?,
    val body: ByteArray,
)

@Component
class RestTemplateExternalMaterialHttp : ExternalMaterialHttp {
    private val permits = Semaphore(4)
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(Duration.ofSeconds(3))
        .proxy(HttpClient.Builder.NO_PROXY)
        .build()

    private val template = RestTemplate(JdkClientHttpRequestFactory(client).apply {
        setReadTimeout(Duration.ofSeconds(6))
    }).apply {
        // Read statuses directly so the default error handler does not buffer error bodies.
        errorHandler = NoOpResponseErrorHandler()
    }

    override fun get(uri: URI, headers: Map<String, String>, maxBytes: Int): ExternalHttpResponse {
        if (!permits.tryAcquire()) throw IOException("External request capacity exceeded")
        try {
            return template.execute(uri, HttpMethod.GET, { request ->
                headers.forEach { (name, value) -> request.headers.set(name, value) }
                request.headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip")
            }, { response ->
                val status = response.statusCode.value()
                val body = response.body.use { stream ->
                    if (status != 200) {
                        byteArrayOf()
                    } else {
                        if (response.headers.contentLength > maxBytes) throw IOException("Response exceeds size limit")
                        val gzip = response.headers.getFirst(HttpHeaders.CONTENT_ENCODING).equals("gzip", ignoreCase = true)
                        val data = if (gzip) GZIPInputStream(stream).use { it.readNBytes(maxBytes + 1) }
                            else stream.readNBytes(maxBytes + 1)
                        if (data.size > maxBytes) throw IOException("Response exceeds size limit")
                        data
                    }
                }
                ExternalHttpResponse(status, response.headers.getFirst(HttpHeaders.CONTENT_TYPE), body)
            }) ?: throw IOException("External response was missing")
        } catch (_: RestClientException) {
            throw IOException("External request failed")
        } finally {
            permits.release()
        }
    }

    @PreDestroy
    fun close() {
        client.shutdownNow()
    }
}
