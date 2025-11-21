package com.example.nunito.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthToken(
    @field:NotBlank
    val accessToken: String,
    val refreshToken: String? = null,
    @field:NotBlank
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600
)

data class TeacherRegistrationRequest(
    @field:NotBlank
    val name: String,
    @field:Email
    @field:NotBlank
    val email: String,
    @field:Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    val password: String
)

data class TeacherLoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String
)

data class TeacherAuthResponse(
    val teacher: Teacher,
    val token: AuthToken
)
