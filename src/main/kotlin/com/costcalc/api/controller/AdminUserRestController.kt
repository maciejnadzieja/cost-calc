package com.costcalc.api.controller

import com.costcalc.api.dto.AdminUserPage
import com.costcalc.api.service.AdminUserService
import com.costcalc.api.service.DirectoryUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserRestController(private val users: AdminUserService) {
    @GetMapping
    fun list(@RequestParam(defaultValue = "0") page: Int): AdminUserPage {
        if (page < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative")
        return users.page(page)
    }

    @ExceptionHandler(DirectoryUnavailableException::class)
    fun unavailable(): ResponseEntity<Map<String, String>> = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(mapOf("code" to "DIRECTORY_UNAVAILABLE", "message" to "User directory is temporarily unavailable"))

    @ExceptionHandler(ResponseStatusException::class, MethodArgumentTypeMismatchException::class)
    fun invalidPage(): ResponseEntity<Map<String, String>> = ResponseEntity.badRequest()
        .body(mapOf("code" to "INVALID_PAGE", "message" to "Page must be a non-negative integer"))
}
