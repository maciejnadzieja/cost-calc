package com.costcalc.api.search

import jakarta.annotation.PreDestroy
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.SystemDefaultDnsResolver
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.util.Timeout
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun interface ExternalMaterialHttp {
    @Throws(IOException::class)
    fun get(uri: URI, headers: Map<String, String>, maxBytes: Int): ExternalHttpResponse
}

data class ExternalHttpResponse(
    val status: Int,
    val contentType: String?,
    val location: String?,
    val body: ByteArray,
)

@Component
class ApacheExternalMaterialHttp : ExternalMaterialHttp {
    private val workers = ThreadPoolExecutor(4, 4, 0L, TimeUnit.SECONDS, SynchronousQueue()) { task ->
        Thread(task, "external-material-http").apply { isDaemon = true }
    }
    private val client = HttpClients.custom()
        .setConnectionManager(
            PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(PublicExternalDnsResolver())
                .setMaxConnTotal(4)
                .setMaxConnPerRoute(4)
                .setDefaultConnectionConfig(
                    ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(3))
                        .setSocketTimeout(Timeout.ofSeconds(4)).build(),
                ).build(),
        )
        .setDefaultRequestConfig(
            RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(4)).build(),
        )
        .disableRedirectHandling()
        .disableCookieManagement()
        .disableAutomaticRetries()
        .build()

    override fun get(uri: URI, headers: Map<String, String>, maxBytes: Int): ExternalHttpResponse {
        val request = HttpGet(uri)
        headers.forEach { (name, value) -> request.setHeader(name, value) }
        val pending = try {
            workers.submit<ExternalHttpResponse> {
                client.executeOpen(null, request, null).use { response ->
                    val entity = response.entity
                    val body = if (response.code == 200 && entity != null) {
                        if (entity.contentLength > maxBytes) {
                            request.cancel()
                            throw IOException("Response exceeds size limit")
                        }
                        entity.content.readNBytes(maxBytes + 1).also {
                            if (it.size > maxBytes) {
                                request.cancel()
                                throw IOException("Response exceeds size limit")
                            }
                        }
                    } else {
                        // There is no reason to read an untrusted redirect or error body.
                        request.cancel()
                        byteArrayOf()
                    }
                    ExternalHttpResponse(response.code, entity?.contentType, response.getFirstHeader("Location")?.value, body)
                }
            }
        } catch (_: RejectedExecutionException) {
            throw IOException("External request capacity exceeded")
        }
        try {
            return pending.get(6, TimeUnit.SECONDS)
        } catch (_: TimeoutException) {
            throw IOException("External request timed out")
        } catch (_: ExecutionException) {
            throw IOException("External request failed")
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("External request interrupted")
        } finally {
            if (!pending.isDone) {
                request.cancel()
                pending.cancel(true)
            }
        }
    }

    @PreDestroy
    fun close() {
        workers.shutdownNow()
        client.close()
    }
}

internal class PublicExternalDnsResolver : SystemDefaultDnsResolver() {
    override fun resolve(host: String): Array<InetAddress> =
        super.resolve(host).also { addresses ->
            if (addresses.isEmpty() || addresses.any { !isPublic(it) }) {
                throw UnknownHostException("External source does not resolve exclusively to public addresses")
            }
        }

    companion object {
        internal fun isPublic(address: InetAddress): Boolean {
            if (address.isAnyLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress ||
                address.isSiteLocalAddress || address.isMulticastAddress
            ) return false

            val octets = address.address.map { it.toInt() and 255 }
            val (a, b, c, d) = octets
            if (octets.size == 4) {
                return a in 1..223 && !(a == 100 && b in 64..127) &&
                    !(a == 192 && (b == 0 && c in setOf(0, 2) || b == 88 && c == 99)) &&
                    !(a == 198 && (b in 18..19 || b == 51 && c == 100)) &&
                    !(a == 203 && b == 0 && c == 113)
            }
            // Global unicast only. Exclude transition, documentation and special-purpose ranges.
            return a in 0x20..0x3f && !(a == 0x20 && b == 0x02) &&
                !(a == 0x20 && b == 0x01 && (c <= 1 || c == 0x0d && d == 0xb8)) &&
                !(a == 0x3f && b == 0xff)
        }
    }
}
