package com.example.nunito.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class StudentSummary(
    val id: UUID,
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String? = null
)

data class StudentJoinRequest(
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String? = null
)

data class StudentSession(
    val room: Room,
    val student: StudentSummary,
    val sessionToken: String? = null
)

data class GameResultSubmission(
    val studentId: UUID? = null,
    @field:NotBlank
    val studentName: String,
    val roomId: UUID? = null,
    @field:NotNull
    val gameId: GameId,
    @field:Min(1)
    val totalQuestions: Int,
    @field:Min(0)
    val correctAnswers: Int,
    @field:Min(0)
    val incorrectAnswers: Int,
    val averageTimeSeconds: Double,
    val score: Double,
    val completedAt: Instant? = null
)

data class StudentResult(
    val id: UUID,
    val studentId: UUID?,
    val studentName: String,
    val roomId: UUID,
    val gameId: GameId,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val averageTimeSeconds: Double,
    val score: Double,
    val completedAt: Instant
)
