package com.example.nunito.model

import java.time.Instant

data class ValidationErrorDetail(
    val field: String,
    val message: String
)

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<ValidationErrorDetail>? = null,
    val path: String? = null,
    val timestamp: Instant = Instant.now()
)
