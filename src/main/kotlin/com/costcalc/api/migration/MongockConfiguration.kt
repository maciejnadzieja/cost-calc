package com.costcalc.api.migration

import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver
import io.mongock.runner.springboot.MongockSpringboot
import io.mongock.runner.springboot.base.MongockApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongockConfiguration {
    @Bean
    fun mongockRunner(
        mongoTemplate: MongoTemplate,
        applicationContext: ApplicationContext,
    ): MongockApplicationRunner =
        MongockSpringboot.builder()
            .setDriver(SpringDataMongoV4Driver.withDefaultLock(mongoTemplate))
            .addMigrationScanPackage("com.costcalc.api.migration")
            .setTransactionEnabled(false)
            .setSpringContext(applicationContext)
            .buildApplicationRunner()
}
