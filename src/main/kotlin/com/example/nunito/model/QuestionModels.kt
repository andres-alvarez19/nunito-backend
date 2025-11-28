package com.example.nunito.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class Question(
    val id: UUID,
    val text: String,
    val type: GameId,
    val options: Map<String, Any>?,
    val correctAnswer: String?,
    val testSuiteId: UUID,
    val createdAt: Instant
)

data class CreateQuestionRequest(
    @field:NotBlank
    val text: String,
    @field:NotNull
    val type: GameId,
    val options: Map<String, Any>? = null,
    val correctAnswer: String? = null,
    @field:NotNull
    val testSuiteId: UUID
)

data class UpdateQuestionRequest(
    val text: String? = null,
    val type: GameId? = null,
    val options: Map<String, Any>? = null,
    val correctAnswer: String? = null
)
