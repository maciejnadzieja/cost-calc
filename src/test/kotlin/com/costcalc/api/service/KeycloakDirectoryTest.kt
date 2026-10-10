package com.costcalc.api.service

import com.costcalc.api.config.KeycloakDirectoryProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.test.system.CapturedOutput
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.Level
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestClient
import java.net.SocketTimeoutException

@ExtendWith(OutputCaptureExtension::class)
class KeycloakDirectoryTest {
    private val mapper = ObjectMapper()
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val properties = KeycloakDirectoryProperties().apply {
        clientSecret = "synthetic-test-secret"
        serverUrl = "https://internal.example/auth"
    }
    private val client = KeycloakDirectoryClient(properties, "https://public.example/auth/realms/application", builder.build())
    private val service = AdminUserService(client)

    private fun token() {
        server.expect(requestTo("https://internal.example/auth/realms/application/protocol/openid-connect/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(content().string("grant_type=client_credentials&client_id=cost-calc-directory&client_secret=synthetic-test-secret"))
            .andRespond(withSuccess("""{"access_token":"synthetic-directory-token"}""", MediaType.APPLICATION_JSON))
    }

    private fun batch(first: Int, rows: List<Map<String, Any?>>) {
        server.expect(requestTo("https://internal.example/auth/admin/realms/application/users?first=$first&max=100&briefRepresentation=false"))
            .andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Bearer synthetic-directory-token"))
            .andRespond(withSuccess(mapper.writeValueAsString(rows), MediaType.APPLICATION_JSON))
    }

    private fun human(i: Int): Map<String, Any?> = mapOf(
        "username" to "person-$i", "email" to if (i == 0) null else "person-$i@example.invalid",
        "enabled" to (i % 2 == 0), "credentials" to listOf("must-not-leave-backend"),
    )
    private fun account(i: Int) = mapOf("username" to "worker-$i", "serviceAccountClientId" to "worker", "enabled" to true)

    @Test
    fun `fills first human page and looks beyond entire service account batch`() {
        token()
        batch(0, (0 until 20).map(::human) + (0 until 80).map(::account))
        batch(100, (80 until 180).map(::account))
        batch(200, listOf(human(20)))
        val page = service.page(0)
        assertThat(page.content.map { it.username }).containsExactlyElementsOf((0 until 20).map { "person-$it" })
        assertThat(page.hasNext).isTrue()
        assertThat(page.content[0].email).isNull()
        assertThat(page.content[1].enabled).isFalse()
        assertThat(mapper.writeValueAsString(page)).doesNotContain("credentials", "must-not-leave-backend", "token", "secret")
        server.verify()
    }

    @Test
    fun `second page skips humans not upstream offsets and includes final rows`() {
        token()
        batch(0, (0 until 100).map { if (it % 5 == 0) human(it / 5) else account(it) })
        batch(100, listOf(account(100), human(20), account(101), human(21)))
        val page = service.page(1)
        assertThat(page.content.map { it.username }).containsExactly("person-20", "person-21")
        assertThat(page.hasNext).isFalse()
        assertThat(page.number).isEqualTo(1)
        server.verify()
    }

    @Test
    fun `empty directory and beyond end return empty final pages`() {
        token()
        batch(0, emptyList())
        assertThat(service.page(0).content).isEmpty()
        server.verify()
    }

    @Test
    fun `beyond last human and only services is empty`() {
        token()
        batch(0, (0 until 100).map(::account))
        batch(100, listOf(human(0)))
        val page = service.page(Int.MAX_VALUE)
        assertThat(page.content).isEmpty()
        assertThat(page.hasNext).isFalse()
        server.verify()
    }

    @Test
    fun `exactly twenty humans do not offer a phantom next page`() {
        token()
        batch(0, (0 until 20).map(::human))
        assertThat(service.page(0).hasNext).isFalse()
        server.verify()
    }

    @Test
    fun `missing credentials fails safely without HTTP`() {
        properties.clientSecret = ""
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @ParameterizedTest
    @ValueSource(ints = [401, 403, 500, 503])
    fun `upstream errors never expose raw body or causes`(status: Int) {
        token()
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status)).body("sensitive upstream body"))
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @Test
    fun `token failure and timeout fail safely`() {
        server.expect(anything()).andRespond(withException(SocketTimeoutException("sensitive timeout details")))
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @Test
    fun `malformed token fails safely`() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON))
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @Test
    fun `malformed directory rows cannot masquerade as disabled humans`() {
        token()
        batch(0, listOf(mapOf("username" to "human")))
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @Test
    fun `malformed directory body fails safely`() {
        token()
        server.expect(anything()).andRespond(withSuccess("{\"access_token\":\"sensitive\"}", MediaType.APPLICATION_JSON))
        assertUnavailable { service.page(0) }
        server.verify()
    }

    @Test
    fun `server falls back to issuer base and configured realm`() {
        val props = KeycloakDirectoryProperties().apply { clientSecret = "synthetic-test-secret" }
        val directory = KeycloakDirectoryClient(props, "https://public.example/auth/realms/application/", builder.build())
        server.expect(requestTo("https://public.example/auth/realms/application/protocol/openid-connect/token"))
            .andRespond(withSuccess("{\"access_token\":\"test\"}", MediaType.APPLICATION_JSON))
        assertThat(directory.token()).isEqualTo("test")
        server.verify()
    }

    @Test
    fun `missing issuer disables directory without HTTP or breaking unrelated startup`() {
        val directory = KeycloakDirectoryClient(properties, "", builder.build())
        assertUnavailable { directory.token() }
        server.verify()
    }

    @Test
    fun `absent and blank emails and human service-like usernames are preserved`() {
        token()
        batch(0, listOf(
            mapOf("username" to "service-account-human", "enabled" to true),
            mapOf("username" to "ordinary", "email" to "", "enabled" to true, "serviceAccountClientId" to null),
        ))
        val page = service.page(0)
        assertThat(page.content.map { it.username }).containsExactly("service-account-human", "ordinary")
        assertThat(page.content.map { it.email }).containsOnlyNulls()
        server.verify()
    }

    @Test
    fun `debug HTTP logging does not print client secret or directory token`(output: CapturedOutput) {
        val logger = LoggerFactory.getLogger("org.springframework.web.client.DefaultRestClient") as Logger
        val previous = logger.level
        try {
            logger.level = Level.DEBUG
            token()
            batch(0, emptyList())
            service.page(0)
            assertThat(output.all).doesNotContain("synthetic-test-secret", "synthetic-directory-token")
            server.verify()
        } finally {
            logger.level = previous
        }
    }

    @Test
    fun `reserved characters in client credentials are form encoded`() {
        properties.clientId = "directory+client"
        properties.clientSecret = "test&pass ="
        server.expect(anything())
            .andExpect(content().string("grant_type=client_credentials&client_id=directory%2Bclient&client_secret=test%26pass+%3D"))
            .andRespond(withSuccess("{\"access_token\":\"test\"}", MediaType.APPLICATION_JSON))
        assertThat(client.token()).isEqualTo("test")
        server.verify()
    }

    private fun assertUnavailable(action: () -> Any) {
        assertThatThrownBy { action() }.isInstanceOf(DirectoryUnavailableException::class.java)
            .hasMessage("User directory is unavailable").hasNoCause()
    }
}
