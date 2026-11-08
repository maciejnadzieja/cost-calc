package com.costcalc.api.search

import com.sun.net.httpserver.HttpServer
import org.apache.hc.client5.http.SystemDefaultDnsResolver
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
    fun rejectsPrivateResolutionAndBoundsChunkedDecompressedBodies() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val requests = AtomicInteger()
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
        val uri = URI("http://localhost:" + server.address.port + "/sample")
        val protected = ApacheExternalMaterialHttp()
        val fixtureClient = ApacheExternalMaterialHttp(SystemDefaultDnsResolver())
        try {
            assertThrows(IOException::class.java) { protected.get(uri, emptyMap(), 4_096) }
            assertEquals(0, requests.get())
            assertEquals(2_048, fixtureClient.get(uri, emptyMap(), 4_096).body.size)
            assertThrows(IOException::class.java) { fixtureClient.get(uri, emptyMap(), 128) }
            assertEquals(2, requests.get())
        } finally {
            protected.close()
            fixtureClient.close()
            server.stop(0)
        }
    }
}
