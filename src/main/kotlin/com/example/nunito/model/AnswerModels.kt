package com.example.nunito.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import com.example.nunito.model.GameId
import java.time.Instant
import java.util.UUID

data class AnswerSubmission(
    val studentId: UUID,
    val gameId: GameId? = null,
    @field:NotBlank
    val questionId: String,
    val questionText: String? = null,
    @field:NotBlank
    val answer: String,
    val isCorrect: Boolean? = null,
    val elapsedMs: Long? = null,
    @field:Min(1)
    val attempt: Int = 1,
    val sentAt: Instant? = null,
    val replace: Boolean = false
)

data class AnswerRecord(
    val id: UUID,
    val roomId: UUID,
    val studentId: UUID,
    val gameId: GameId?,
    val questionId: String,
    val questionText: String?,
    val answer: String,
    val isCorrect: Boolean?,
    val elapsedMs: Long?,
    val attempt: Int,
    val createdAt: Instant,
    val sentAt: Instant?
)
