package com.example.nunito.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class Room(
    val id: UUID,
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val name: String,
    @field:NotNull
    val gameId: GameId,
    @field:NotNull
    val difficulty: Difficulty,
    @field:Min(1)
    val durationMinutes: Int,
    val isActive: Boolean,
    @field:NotNull
    val status: RoomStatus,
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
    val gameId: GameId,
    val difficulty: Difficulty,
    val durationMinutes: Int,
    val isActive: Boolean,
    val status: RoomStatus,
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
    @field:NotNull
    val gameId: GameId,
    @field:NotNull
    val difficulty: Difficulty,
    @field:Min(1)
    val durationMinutes: Int
)

data class UpdateRoomRequest(
    val name: String? = null,
    val gameId: GameId? = null,
    val difficulty: Difficulty? = null,
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
