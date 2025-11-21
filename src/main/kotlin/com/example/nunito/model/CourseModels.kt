package com.example.nunito.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class Course(
    val id: UUID,
    val name: String,
    val description: String?,
    val teacherId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

data class CreateCourseRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotNull
    val teacherId: UUID
)

data class UpdateCourseRequest(
    val name: String? = null,
    val description: String? = null
)
