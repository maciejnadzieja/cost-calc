package com.costcalc.api.migration

import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.testcontainers.containers.MongoDBContainer

class BackfillSearchFieldsMigrationTest {
    @Test
    fun `mongock backfills legacy documents and records the migration`() {
        MongoDBContainer("mongo:7.0").use { container ->
            container.start()
            val databaseName = "issue24_mongock_migration"
            MongoClients.create(container.getReplicaSetUrl(databaseName)).use { client ->
                val mongoTemplate = MongoTemplate(SimpleMongoClientDatabaseFactory(client, databaseName))
                val database = mongoTemplate.db
                database.getCollection("services").insertOne(
                    Document("_id", "legacy-service").append("name", "Łódź").append("description", "Café"),
                )
                database.getCollection("services").insertOne(
                    Document("_id", "partial-service").append("name", "Łódź").append("description", "Café")
                        .append("searchName", "existing-value"),
                )
                database.getCollection("materials").insertOne(
                    Document("_id", "legacy-material").append("name", "Düsseldorf").append("description", "Crème brûlée"),
                )

                GenericApplicationContext().use { context ->
                    context.beanFactory.registerSingleton("mongoTemplate", mongoTemplate)
                    context.refresh()
                    MongockConfiguration().mongockRunner(mongoTemplate, context).run(DefaultApplicationArguments())
                }

                val service = database.getCollection("services").find(Filters.eq("_id", "legacy-service")).first()
                val partialService = database.getCollection("services").find(Filters.eq("_id", "partial-service")).first()
                val material = database.getCollection("materials").find(Filters.eq("_id", "legacy-material")).first()

                assertEquals("lodz", service?.getString("searchName"))
                assertEquals("cafe", service?.getString("searchDescription"))
                assertEquals("existing-value", partialService?.getString("searchName"))
                assertEquals("cafe", partialService?.getString("searchDescription"))
                assertEquals("dusseldorf", material?.getString("searchName"))
                assertEquals("creme brulee", material?.getString("searchDescription"))
                assertNotNull(database.getCollection("mongockChangeLog").find(Filters.eq("changeId", "backfill-search-fields")).first())
            }
        }
    }
}
