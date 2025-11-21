package com.example.nunito.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class Teacher(
    val id: UUID,
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val email: String,
    val createdAt: Instant = Instant.now()
)

data class TeacherSettingsFeedback(
    val sounds: Boolean = true,
    val animations: Boolean = true,
    val hints: Boolean = true
)

data class TeacherSettings(
    @field:NotNull
    val availableGames: Map<String, Boolean>,
    @field:NotNull
    val defaultDifficulty: Difficulty,
    @field:Min(10)
    val questionTimeSeconds: Int,
    @field:NotNull
    val feedback: TeacherSettingsFeedback
)

data class TeacherMetrics(
    val activeRooms: Int,
    val connectedStudents: Int,
    val completedActivities: Int,
    val averageScore: Double
)

data class TeacherDashboardStats(
    val activeRoomsCount: Int,
    val connectedStudentsCount: Int,
    val completedActivitiesCount: Int,
    val averageProgress: Int
)

data class RecentRoomSummary(
    val id: UUID,
    val title: String,
    val gameLabel: String,
    val difficulty: String,
    val createdAt: Instant,
    val students: Int,
    val average: Int,
    val status: String
)
