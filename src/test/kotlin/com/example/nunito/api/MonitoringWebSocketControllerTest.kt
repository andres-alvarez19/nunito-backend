package com.example.nunito.api

import com.example.nunito.model.AnswerEventDto
import com.example.nunito.model.GlobalMonitoringStatsDto
import com.example.nunito.model.MonitoringSnapshotDto
import com.example.nunito.model.RankingEntryDto
import com.example.nunito.model.StudentMonitoringStateDto
import com.example.nunito.service.AnswerService
import com.example.nunito.service.MonitoringService
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessagingTemplate

@ExtendWith(MockitoExtension::class)
class MonitoringWebSocketControllerTest {

    @Mock
    private lateinit var answerService: AnswerService

    @Mock
    private lateinit var monitoringService: MonitoringService

    @Mock
    private lateinit var messagingTemplate: SimpMessagingTemplate

    private lateinit var controller: MonitoringWebSocketController

    @BeforeEach
    fun setup() {
        controller = MonitoringWebSocketController(answerService, monitoringService, messagingTemplate)
    }

    @Test
    fun `handleAnswer persists and publishes snapshot`() {
        val roomId = UUID.randomUUID().toString()
        val event = AnswerEventDto(
            roomId = roomId,
            studentId = UUID.randomUUID().toString(),
            studentName = "Student",
            gameId = "game",
            questionId = "q1",
            questionText = "text",
            selectedOptionId = "optA",
            selectedOptionText = "Option A",
            isCorrect = true,
            elapsedMillis = 100,
            answeredAt = Instant.now()
        )
        val snapshot = MonitoringSnapshotDto(
            roomId = roomId,
            students = listOf(
                StudentMonitoringStateDto(
                    studentId = event.studentId,
                    studentName = event.studentName,
                    currentGameId = event.gameId,
                    currentQuestionId = event.questionId,
                    currentQuestionText = event.questionText,
                    lastSelectedOptionId = event.selectedOptionId,
                    lastSelectedOptionText = event.selectedOptionText,
                    lastIsCorrect = event.isCorrect,
                    totalAnswered = 1,
                    totalCorrect = 1,
                    accuracyPct = 100.0,
                    avgResponseMillis = event.elapsedMillis.toDouble()
                )
            ),
            global = GlobalMonitoringStatsDto(
                totalAnsweredAll = 1,
                totalCorrectAll = 1,
                globalAccuracyPct = 100.0
            ),
            ranking = listOf(
                RankingEntryDto(
                    studentId = event.studentId,
                    studentName = event.studentName,
                    totalAnswered = 1,
                    avgResponseMillis = event.elapsedMillis.toDouble(),
                    rank = 1
                )
            )
        )

        whenever(monitoringService.registerAnswer(roomId, event)).thenReturn(snapshot)

        controller.handleAnswer(roomId, event)

        verify(answerService).submit(eq(UUID.fromString(roomId)), any())
        verify(messagingTemplate).convertAndSend("/topic/monitoring/room/$roomId/snapshot", snapshot)
        verify(messagingTemplate).convertAndSend("/topic/monitoring/room/$roomId/answers", event)
    }
}
