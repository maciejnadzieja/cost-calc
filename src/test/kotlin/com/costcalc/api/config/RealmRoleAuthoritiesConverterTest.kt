package com.costcalc.api.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class RealmRoleAuthoritiesConverterTest {
    private val converter = RealmRoleAuthoritiesConverter()

    @Test
    fun `preserves scopes and grants only realm roles`() {
        val jwt = Jwt.withTokenValue("test").header("alg", "none")
            .claim("scope", "openid email")
            .claim("realm_access", mapOf("roles" to listOf("USER", "ADMIN", 123)))
            .claim("resource_access", mapOf("client" to mapOf("roles" to listOf("OWNER"))))
            .build()
        assertThat(converter.convert(jwt).map { it.authority })
            .containsExactly("SCOPE_openid", "SCOPE_email", "ROLE_USER", "ROLE_ADMIN")
    }

    @Test
    fun `missing malformed or client role claims do not grant ADMIN`() {
        listOf(null, "ADMIN", mapOf("roles" to "ADMIN"), mapOf("roles" to listOf("admin"))).forEach { realm ->
            val builder = Jwt.withTokenValue("test").header("alg", "none").subject("human")
                .claim("resource_access", mapOf("client" to mapOf("roles" to listOf("ADMIN"))))
            if (realm != null) builder.claim("realm_access", realm)
            assertThat(converter.convert(builder.build()).map { it.authority }).doesNotContain("ROLE_ADMIN")
        }
    }
}
