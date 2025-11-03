package com.costcalc.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO

@SpringBootApplication
@EnableMongoRepositories
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class CostCalcApplication

fun main(args: Array<String>) {
    runApplication<CostCalcApplication>(*args)
}
