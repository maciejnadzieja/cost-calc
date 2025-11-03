package com.costcalc.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(
        ex: ForbiddenException,
        request: WebRequest,
    ): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
}
