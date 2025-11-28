package com.example.nunito.service

import com.example.nunito.model.AnswerEventDto
import com.example.nunito.model.GlobalMonitoringStatsDto
import com.example.nunito.model.MonitoringSnapshotDto
import com.example.nunito.model.RankingEntryDto
import com.example.nunito.model.StudentMonitoringStateDto
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service

@Service
class MonitoringService {

    private val roomStates: MutableMap<String, RoomMonitoringState> = ConcurrentHashMap()

    fun registerAnswer(roomId: String, event: AnswerEventDto): MonitoringSnapshotDto {
        val roomState = roomStates.computeIfAbsent(roomId) { RoomMonitoringState(roomId) }
        return roomState.recordAnswer(event)
    }

    fun resetRoomMonitoring(roomId: String): MonitoringSnapshotDto {
        val roomState = RoomMonitoringState(roomId)
        roomStates[roomId] = roomState
        return roomState.buildSnapshot()
    }

    private class RoomMonitoringState(private val roomId: String) {
        private val students: MutableMap<String, StudentState> = ConcurrentHashMap()
        private var totalAnsweredAll: Long = 0
        private var totalCorrectAll: Long = 0

        fun recordAnswer(event: AnswerEventDto): MonitoringSnapshotDto = synchronized(this) {
            val studentState = students.computeIfAbsent(event.studentId) {
                StudentState(studentId = event.studentId, studentName = event.studentName)
            }
            studentState.update(event)
            totalAnsweredAll += 1
            if (event.isCorrect) {
                totalCorrectAll += 1
            }
            buildSnapshotLocked()
        }

        fun buildSnapshot(): MonitoringSnapshotDto = synchronized(this) {
            buildSnapshotLocked()
        }

        private fun buildSnapshotLocked(): MonitoringSnapshotDto {
            val studentDtos = students.values
                .map { it.toDto() }
                .sortedBy { it.studentName }

            val globalAccuracyPct = if (totalAnsweredAll > 0) {
                (totalCorrectAll.toDouble() / totalAnsweredAll.toDouble()) * 100.0
            } else {
                0.0
            }

            val ranking = students.values
                .sortedWith(
                    compareByDescending<StudentState> { it.totalAnswered }
                        .thenBy { it.avgResponseMillis() }
                )
                .mapIndexed { index, student ->
                    RankingEntryDto(
                        studentId = student.studentId,
                        studentName = student.studentName,
                        totalAnswered = student.totalAnswered,
                        avgResponseMillis = student.avgResponseMillis(),
                        rank = index + 1
                    )
                }

            return MonitoringSnapshotDto(
                roomId = roomId,
                students = studentDtos,
                global = GlobalMonitoringStatsDto(
                    totalAnsweredAll = totalAnsweredAll,
                    totalCorrectAll = totalCorrectAll,
                    globalAccuracyPct = globalAccuracyPct
                ),
                ranking = ranking
            )
        }
    }

    private data class StudentState(
        val studentId: String,
        var studentName: String,
        var currentGameId: String = "",
        var currentQuestionId: String? = null,
        var currentQuestionText: String? = null,
        var lastSelectedOptionId: String? = null,
        var lastSelectedOptionText: String? = null,
        var lastIsCorrect: Boolean? = null,
        var totalAnswered: Int = 0,
        var totalCorrect: Int = 0,
        var totalElapsedMillis: Long = 0
    ) {
        fun update(event: AnswerEventDto) {
            studentName = event.studentName
            currentGameId = event.gameId
            currentQuestionId = event.questionId
            currentQuestionText = event.questionText
            lastSelectedOptionId = event.selectedOptionId
            lastSelectedOptionText = event.selectedOptionText
            lastIsCorrect = event.isCorrect
            totalAnswered += 1
            if (event.isCorrect) {
                totalCorrect += 1
            }
            totalElapsedMillis += event.elapsedMillis
        }

        fun toDto(): StudentMonitoringStateDto {
            val accuracyPct = if (totalAnswered > 0) {
                (totalCorrect.toDouble() / totalAnswered.toDouble()) * 100.0
            } else {
                0.0
            }
            return StudentMonitoringStateDto(
                studentId = studentId,
                studentName = studentName,
                currentGameId = currentGameId,
                currentQuestionId = currentQuestionId,
                currentQuestionText = currentQuestionText,
                lastSelectedOptionId = lastSelectedOptionId,
                lastSelectedOptionText = lastSelectedOptionText,
                lastIsCorrect = lastIsCorrect,
                totalAnswered = totalAnswered,
                totalCorrect = totalCorrect,
                accuracyPct = accuracyPct,
                avgResponseMillis = avgResponseMillis()
            )
        }

        fun avgResponseMillis(): Double {
            return if (totalAnswered > 0) {
                totalElapsedMillis.toDouble() / totalAnswered.toDouble()
            } else {
                0.0
            }
        }
    }
}
