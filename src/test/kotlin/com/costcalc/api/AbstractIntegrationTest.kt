package com.costcalc.api

import com.costcalc.api.config.TestSecurityConfig
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Import(TestSecurityConfig::class)
@AutoConfigureMockMvc
@Testcontainers
abstract class AbstractIntegrationTest {

    companion object {
        private val mongoDBContainer = MongoDBContainer("mongo:7.0")

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            mongoDBContainer.start()
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }
}