package com.example.nunito.model

import java.time.Instant
import java.util.UUID

data class RoomReportSummary(
    val roomId: UUID,
    val roomName: String,
    val games: List<GameInfo>,
    val difficulty: Difficulty,
    val studentsCount: Int,
    val averageScore: Double,
    val completionRate: Double,
    val createdAt: Instant
)

data class RoomReport(
    val roomId: UUID,
    val roomName: String,
    val games: List<GameInfo>,
    val difficulty: Difficulty,
    val studentsCount: Int,
    val averageScore: Double,
    val completionRate: Double,
    val createdAt: Instant,
    val students: List<StudentResult>
)

data class RoomFullResults(
    val roomId: UUID,
    val students: List<StudentResult>
)
