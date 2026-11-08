package com.costcalc.api.migration

import com.costcalc.api.search.AccentFolding
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.mongodb.core.MongoTemplate

/** One-time, idempotent backfill for records created before normalized search fields existed. */
@ChangeUnit(id = "backfill-search-fields", order = "001", author = "cost-calc")
class BackfillSearchFieldsChangeUnit {
    @Execution
    fun execute(mongoTemplate: MongoTemplate) {
        for (collectionName in listOf("services", "materials")) {
            backfill(mongoTemplate, collectionName, "searchName", "name")
            backfill(mongoTemplate, collectionName, "searchDescription", "description")
        }
    }

    @RollbackExecution
    fun rollback() {
        // Additive fields are safe to retain if the change unit is retried.
    }

    private fun backfill(
        mongoTemplate: MongoTemplate,
        collectionName: String,
        searchField: String,
        sourceField: String,
    ) {
        val collection = mongoTemplate.db.getCollection(collectionName)
        val missing = Filters.exists(searchField, false)
        val records =
            collection.find(missing)
                .projection(Projections.include("_id", sourceField))

        for (record in records) {
            val sourceValue = record.getString(sourceField) ?: ""
            val unchanged =
                Filters.and(
                    Filters.eq("_id", record["_id"]),
                    Filters.eq(sourceField, record[sourceField]),
                    missing,
                )
            collection.updateOne(
                unchanged,
                Updates.set(searchField, AccentFolding.fold(sourceValue)),
            )
        }
    }
}
