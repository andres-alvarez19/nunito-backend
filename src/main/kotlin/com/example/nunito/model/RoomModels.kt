package com.example.nunito.model

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class Room(
    val id: UUID,
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val name: String,
    val games: List<GameInfo>,
    @field:NotNull
    val difficulty: Difficulty,
    @field:Min(1)
    val durationMinutes: Int,
    val isActive: Boolean,
    @field:NotNull
    val status: RoomStatus,
    @field:NotNull
    val testSuiteId: UUID,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val teacherId: UUID,
    val teacher: Teacher? = null,
    val students: List<StudentSummary> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

data class RoomSummary(
    val id: UUID,
    val code: String,
    val name: String,
    val games: List<GameInfo>,
    val difficulty: Difficulty,
    val durationMinutes: Int,
    val isActive: Boolean,
    val status: RoomStatus,
    val testSuiteId: UUID,
    val studentsCount: Int,
    val averageScore: Double? = null,
    val completionRate: Double? = null,
    val createdAt: Instant,
    val lastActivityAt: Instant? = null
)

data class CreateRoomRequest(
    @field:NotNull
    val teacherId: UUID,
    @field:NotBlank
    val name: String,
    @field:NotEmpty
    @field:JsonAlias("gameIds")
    val games: List<GameId>,
    @field:NotNull
    val difficulty: Difficulty,
    @field:NotNull
    val testSuiteId: UUID,
    @field:JsonAlias("duration")
    @field:NotNull(message = "El campo duration o durationMinutes es obligatorio")
    @field:Min(1)
    val durationMinutes: Int?
)

data class UpdateRoomRequest(
    val name: String? = null,
    @field:Size(min = 1, message = "Debe haber al menos un juego")
    val gameIds: List<GameId>? = null,
    val difficulty: Difficulty? = null,
    val testSuiteId: UUID? = null,
    @field:Min(1)
    val durationMinutes: Int? = null,
    val isActive: Boolean? = null
)

data class RoomStatusUpdate(
    @field:NotNull
    val status: RoomStatus,
    val isActive: Boolean? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null
)

data class GameInfo(
    val id: GameId,
    val name: String
)
