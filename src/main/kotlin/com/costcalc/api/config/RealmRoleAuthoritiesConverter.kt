package com.costcalc.api.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

/** Only Keycloak realm roles grant application roles; client roles never do. */
class RealmRoleAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    private val scopes = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val roles = realmAccess?.get("roles") as? Collection<*> ?: emptyList<Any>()
        return scopes.convert(jwt).orEmpty() + roles.filterIsInstance<String>()
            .map { SimpleGrantedAuthority("ROLE_$it") }
    }
}
