package com.costcalc.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@ConfigurationProperties("app.keycloak-directory")
class KeycloakDirectoryProperties {
    var clientId: String = "cost-calc-directory"
    var clientSecret: String = ""
    var serverUrl: String = ""
}

@Configuration
@EnableConfigurationProperties(KeycloakDirectoryProperties::class)
class KeycloakDirectoryConfig {
    @Bean
    fun directoryRestClient(builder: RestClient.Builder): RestClient {
        val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER).build()
        val factory = JdkClientHttpRequestFactory(http)
        factory.setReadTimeout(Duration.ofSeconds(3))
        return builder.clone().requestFactory(factory).build()
    }
}
