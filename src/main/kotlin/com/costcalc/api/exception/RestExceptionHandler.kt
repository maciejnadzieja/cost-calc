package com.costcalc.api.exception

import com.costcalc.api.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class RestExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }
        
        logger.warn { "Validation failed: $fieldErrors" }
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Failed",
                    message = "Invalid input data",
                    path = extractPath(request),
                    fieldErrors = fieldErrors,
                ),
            )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(
        ex: ForbiddenException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Access forbidden: ${ex.message}" }
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = ex.message ?: "Access denied",
                    path = extractPath(request),
                ),
            )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Authentication failed: ${ex.message}" }
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = ex.message ?: "Authentication required",
                    path = extractPath(request),
                ),
            )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Resource not found: ${ex.message}" }
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Resource not found",
                    path = extractPath(request),
                ),
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid argument: ${ex.message}" }
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = ex.message ?: "Invalid request",
                    path = extractPath(request),
                ),
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Malformed request: ${ex.message}" }
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = "Malformed JSON request",
                    path = extractPath(request),
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "An unexpected error occurred" }
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                    path = extractPath(request),
                ),
            )
    }

    private fun extractPath(request: WebRequest): String =
        request.getDescription(false).removePrefix("uri=")
}
