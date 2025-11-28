package com.example.nunito.config

import com.example.nunito.exception.ApiException
import com.example.nunito.exception.BadRequestException
import com.example.nunito.model.ErrorResponse
import com.example.nunito.model.ValidationErrorDetail
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = exception.status.value(),
            code = exception.code,
            message = exception.message,
            errors = exception.errors,
            path = request.requestURI
        )
        return ResponseEntity.status(exception.status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val validationErrors = exception.bindingResult.fieldErrors.map {
            ValidationErrorDetail(
                field = it.field,
                message = it.defaultMessage ?: "Dato inválido"
            )
        }
        val wrapped = BadRequestException("Los datos enviados no son válidos", validationErrors)
        return handleApiException(wrapped, request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(exception: ConstraintViolationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val validationErrors = exception.constraintViolations.map {
            ValidationErrorDetail(field = it.propertyPath.toString(), message = it.message)
        }
        val wrapped = BadRequestException("Los datos enviados no son válidos", validationErrors)
        return handleApiException(wrapped, request)
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        HttpMessageNotReadableException::class,
        IllegalArgumentException::class
    )
    fun handleBadRequest(exception: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val wrapped = BadRequestException(exception.message ?: "Petición inválida")
        return handleApiException(wrapped, request)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(exception: DataIntegrityViolationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val rootMessage = exception.rootCause?.message ?: exception.message ?: "Error de integridad de datos"
        val wrapped = BadRequestException(rootMessage)
        return handleApiException(wrapped, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "internal_error",
            message = "Ocurrió un error inesperado",
            errors = listOf(ValidationErrorDetail(field = "general", message = exception.message ?: "Error interno")),
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
