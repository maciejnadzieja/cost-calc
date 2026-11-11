package com.costcalc.api.search

import org.springframework.http.HttpStatus

enum class ExternalMaterialSearchError(val status: HttpStatus, val message: String) {
    INVALID_SEARCH(HttpStatus.BAD_REQUEST, "Invalid material search"),
    UNKNOWN_CATEGORY(HttpStatus.NOT_FOUND, "Search category is unavailable"),
    SEARCH_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Online search is unavailable"),
    SEARCH_BUSY(HttpStatus.TOO_MANY_REQUESTS, "Wait before searching again"),
    RESULT_EXPIRED(HttpStatus.NOT_FOUND, "The result is unavailable. Search again."),
    SOURCE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "The selected page is unavailable"),
    UNSUPPORTED_PAGE(HttpStatus.UNPROCESSABLE_ENTITY, "The selected page could not be used as a material draft"),
}

class ExternalMaterialSearchException(val error: ExternalMaterialSearchError) : RuntimeException(error.message)
