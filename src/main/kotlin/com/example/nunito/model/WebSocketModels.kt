package com.example.nunito.model

import java.time.Instant

data class UserDto(
    val userId: String,
    val name: String
)

enum class RoomLiveStatus {
    WAITING,
    STARTED
}

data class RoomStatusDto(
    val roomId: String,
    val status: RoomLiveStatus
)

data class AnswerEventDto(
    val roomId: String,
    val studentId: String,
    val studentName: String,
    val gameId: String,
    val questionId: String,
    val questionText: String,
    val selectedOptionId: String? = null,
    val selectedOptionText: String? = null,
    val isCorrect: Boolean,
    val elapsedMillis: Long,
    val answeredAt: Instant
)

data class StudentMonitoringStateDto(
    val studentId: String,
    val studentName: String,
    val currentGameId: String,
    val currentQuestionId: String?,
    val currentQuestionText: String?,
    val lastSelectedOptionId: String?,
    val lastSelectedOptionText: String?,
    val lastIsCorrect: Boolean?,
    val totalAnswered: Int,
    val totalCorrect: Int,
    val accuracyPct: Double,
    val avgResponseMillis: Double
)

data class RankingEntryDto(
    val studentId: String,
    val studentName: String,
    val totalAnswered: Int,
    val avgResponseMillis: Double,
    val rank: Int
)

data class GlobalMonitoringStatsDto(
    val totalAnsweredAll: Long,
    val totalCorrectAll: Long,
    val globalAccuracyPct: Double
)

data class MonitoringSnapshotDto(
    val roomId: String,
    val students: List<StudentMonitoringStateDto>,
    val global: GlobalMonitoringStatsDto,
    val ranking: List<RankingEntryDto>
)
