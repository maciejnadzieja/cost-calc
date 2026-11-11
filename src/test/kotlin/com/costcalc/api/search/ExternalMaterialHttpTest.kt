package com.costcalc.api.search

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPOutputStream

class ExternalMaterialHttpTest {
    @Test
    fun stopsAtRedirectsAndBoundsChunkedDecompressedBodies() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val requests = AtomicInteger()
        server.createContext("/redirect") { exchange ->
            exchange.responseHeaders.add("Location", "/sample")
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        server.createContext("/sample") { exchange ->
            requests.incrementAndGet()
            val payload = ByteArrayOutputStream().also { buffer ->
                GZIPOutputStream(buffer).use { it.write(ByteArray(2_048) { 'x'.code.toByte() }) }
            }.toByteArray()
            exchange.responseHeaders.add("Content-Encoding", "gzip")
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.use { it.write(payload) }
        }
        server.start()
        val base = URI("http://localhost:" + server.address.port)
        val client = RestTemplateExternalMaterialHttp()
        try {
            val redirect = client.get(base.resolve("/redirect"), emptyMap(), 4_096)
            assertEquals(302, redirect.status)
            assertEquals(0, redirect.body.size)
            assertEquals(0, requests.get())
            assertEquals(2_048, client.get(base.resolve("/sample"), emptyMap(), 4_096).body.size)
            assertThrows(IOException::class.java) { client.get(base.resolve("/sample"), emptyMap(), 128) }
            assertEquals(2, requests.get())
        } finally {
            client.close()
            server.stop(0)
        }
    }
}
