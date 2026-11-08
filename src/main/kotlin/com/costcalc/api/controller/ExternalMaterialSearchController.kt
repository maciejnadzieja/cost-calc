package com.costcalc.api.controller

import com.costcalc.api.config.CurrentUser
import com.costcalc.api.model.User
import com.costcalc.api.search.ExternalMaterialSearch
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/materials/external-search/{category}")
@PreAuthorize("isAuthenticated()")
class ExternalMaterialSearchController(private val search: ExternalMaterialSearch) {
    @GetMapping("/sources")
    fun sources(@PathVariable category: String) = search.sources(category)

    @PostMapping("/results")
    fun results(
        @PathVariable category: String,
        @Valid @RequestBody request: OnlineMaterialSearchRequest,
        @CurrentUser user: User,
    ) = search.search(user.id, category, request.query, request.sourceIds)

    @PostMapping("/results/{resultId}/details")
    fun details(
        @PathVariable category: String,
        @PathVariable resultId: UUID,
        @CurrentUser user: User,
    ) = search.details(user.id, category, resultId)
}

data class OnlineMaterialSearchRequest(
    @field:NotBlank @field:Size(max = 120) val query: String,
    @field:Size(max = 10) val sourceIds: List<String>? = null,
)
