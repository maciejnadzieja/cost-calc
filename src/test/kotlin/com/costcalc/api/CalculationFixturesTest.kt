package com.costcalc.api

import com.costcalc.api.config.TestSecurityConfig
import com.costcalc.api.model.ItemType
import com.costcalc.api.repository.CalculationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.MountableFile
import java.math.BigDecimal
import java.nio.file.Paths
import java.time.Duration
import java.util.UUID

@SpringBootTest(properties = ["spring.data.mongodb.uuid-representation=standard"])
@AutoConfigureMockMvc
@Import(TestSecurityConfig::class)
@Testcontainers
class CalculationFixturesTest {
    @Autowired private lateinit var repository: CalculationRepository
    @Autowired private lateinit var mongoTemplate: MongoTemplate
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @Test
    fun `fresh Docker database provides mapped tester samples without reseeding existing data`() {
        val testerId = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val realm = objectMapper.readTree(Paths.get("docker/keycloak/realm-config.json").toFile())
        assertThat(realm["users"].first { it["username"].asText() == "tester@cost-calc.eu" }["id"].asText())
            .isEqualTo(testerId.toString())

        val samples = repository.findAll().sortedBy { it.id }
        assertThat(samples).hasSize(3)
        val expectedTotals = listOf("175.00", "185.00", "509.50")
        val expectedStages = listOf(listOf("175.00"), listOf("185.00"), listOf("225.00", "284.50"))
        samples.forEachIndexed { index, sample ->
            assertThat(sample.userId).isEqualTo(testerId)
            assertThat(sample.currency.code).isEqualTo("USD")
            assertThat(sample.totalAmount).isEqualByComparingTo(expectedTotals[index])
            assertThat(sample.stages).hasSize(expectedStages[index].size)
            assertThat(sample.createdAt).isNotNull()
            sample.stages.forEachIndexed { stageIndex, stage ->
                assertThat(stage.totalAmount).isEqualByComparingTo(expectedStages[index][stageIndex])
                stage.services.forEach { assertThat(it.type).isEqualTo(ItemType.SERVICE) }
                stage.materials.forEach { assertThat(it.type).isEqualTo(ItemType.MATERIAL) }
                (stage.services + stage.materials).forEach { item ->
                    assertThat(item.quantity).isGreaterThan(BigDecimal.ZERO)
                    assertThat(item.unitPrice).isGreaterThan(BigDecimal.ZERO)
                    assertThat(item.amount).isEqualByComparingTo(item.quantity.multiply(item.unitPrice))
                }
            }
            mockMvc.get("/api/calculations/{id}", sample.id) {
                with(token(testerId))
            }.andExpect {
                status { isOk() }
                jsonPath("$.totalAmount") { value(expectedTotals[index].toDouble()) }
                jsonPath("$.currency") { value("USD") }
            }
        }
        assertThat(samples[0].stages.single().materials).isEmpty()
        assertThat(samples[1].stages.single().services).isEmpty()
        samples[2].stages.forEach {
            assertThat(it.services).isNotEmpty()
            assertThat(it.materials).isNotEmpty()
        }
        val otherUser = UUID.randomUUID()
        mockMvc.get("/api/calculations") {
            with(token(testerId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(3) }
        }
        mockMvc.get("/api/calculations") {
            with(token(otherUser).authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(0) }
        }
        samples.forEach { sample ->
            mockMvc.get("/api/calculations/{id}", sample.id) {
                with(token(otherUser).authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
            }.andExpect { status { isNotFound() } }
        }

        val existingCounts = listOf("materials", "services", "user_settings").associateWith {
            mongoTemplate.getCollection(it).countDocuments()
        }
        assertThat(existingCounts.values).allMatch { it > 0 }
        val edited = samples.first().copy(description = "Locally edited sample")
        repository.save(edited)

        // Restart only this disposable test container, preserving its existing /data/db.
        mongo.dockerClient.restartContainerCmd(mongo.containerId).withTimeout(10).exec()
        // Docker Desktop can reassign published ports on restart. Query internally instead.
        val sampleIds = samples.joinToString(",") { "UUID('${it.id}')" }
        val persistenceQuery = """
            print(JSON.stringify({
                calculations: db.calculations.countDocuments(),
                originalIds: db.calculations.countDocuments({_id: {${'$'}in: [$sampleIds]}}),
                description: db.calculations.findOne({_id: UUID('${edited.id}')}).description,
                materials: db.materials.countDocuments(),
                services: db.services.countDocuments(),
                user_settings: db.user_settings.countDocuments()
            }));
        """.trimIndent()
        await().atMost(Duration.ofSeconds(60)).untilAsserted {
            val result = mongo.execInContainer(
                "mongosh", "--quiet",
                "mongodb://cost_calc_user:cost_calc_password@localhost:27017/cost_calc?authSource=cost_calc",
                "--eval", persistenceQuery,
            )
            assertThat(result.exitCode).isZero()
            val persisted = objectMapper.readTree(result.stdout)
            assertThat(persisted["calculations"].asInt()).isEqualTo(3)
            assertThat(persisted["originalIds"].asInt()).isEqualTo(3)
            assertThat(persisted["description"].asText()).isEqualTo("Locally edited sample")
            existingCounts.forEach { (collection, count) ->
                assertThat(persisted[collection].asLong()).isEqualTo(count)
            }
        }
    }

    private fun token(userId: UUID) = jwt().jwt {
        it.subject(userId.toString())
        it.claim("email", "$userId@example.invalid")
    }

    private class FixtureMongo : GenericContainer<FixtureMongo>("mongo:7.0")

    companion object {
        @Container
        @JvmStatic
        private val mongo = FixtureMongo()
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_DATABASE", "cost_calc")
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example")
            .withCopyFileToContainer(
                MountableFile.forHostPath(Paths.get("docker/mongodb/init-mongo.js").toAbsolutePath()),
                "/docker-entrypoint-initdb.d/init-mongo.js",
            )
            .waitingFor(
                WaitAllStrategy()
                    .withStrategy(Wait.forLogMessage(".*MongoDB init process complete.*", 1))
                    .withStrategy(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofSeconds(90)),
            )

        @DynamicPropertySource
        @JvmStatic
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            mongo.start()
            registry.add("spring.data.mongodb.uri") {
                "mongodb://cost_calc_user:cost_calc_password@${mongo.host}:${mongo.getMappedPort(27017)}/cost_calc?authSource=cost_calc"
            }
        }
    }
}
