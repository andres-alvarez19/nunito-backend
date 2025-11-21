package com.example.nunito.exception

import com.example.nunito.model.ValidationErrorDetail
import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    val code: String,
    override val message: String,
    val errors: List<ValidationErrorDetail>? = null
) : RuntimeException(message)

class NotFoundException(resource: String, id: String) :
    ApiException(HttpStatus.NOT_FOUND, "not_found", "$resource con id $id no existe")

class ConflictException(message: String) :
    ApiException(HttpStatus.CONFLICT, "conflict", message)

class BadRequestException(message: String, errors: List<ValidationErrorDetail>? = null) :
    ApiException(HttpStatus.BAD_REQUEST, "bad_request", message, errors)

class UnauthorizedException(message: String = "No autorizado") :
    ApiException(HttpStatus.UNAUTHORIZED, "unauthorized", message)
