package com.example.nunito.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class TestSuite(
    val id: UUID,
    val name: String,
    val description: String?,
    val courseId: UUID,
    val games: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

data class CreateTestSuiteRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotNull
    val courseId: UUID,
    val games: List<String> = emptyList()
)

data class UpdateTestSuiteRequest(
    val name: String? = null,
    val description: String? = null,
    val games: List<String>? = null
)
