package com.example.nunito.api

import com.example.nunito.model.AnswerEventDto
import com.example.nunito.service.AnswerService
import com.example.nunito.service.MonitoringService
import com.example.nunito.model.GameId
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class MonitoringWebSocketController(
    private val answerService: AnswerService,
    private val monitoringService: MonitoringService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/monitoring/room/{roomId}/answer")
    fun handleAnswer(
        @DestinationVariable roomId: String,
        @Payload event: AnswerEventDto
    ) {
        persistAnswer(roomId, event)
        val snapshot = monitoringService.registerAnswer(roomId, event)
        messagingTemplate.convertAndSend("/topic/monitoring/room/$roomId/snapshot", snapshot)
        messagingTemplate.convertAndSend("/topic/monitoring/room/$roomId/answers", event)
    }

    @MessageMapping("/monitoring/room/{roomId}/reset")
    fun resetRoom(
        @DestinationVariable roomId: String
    ) {
        val snapshot = monitoringService.resetRoomMonitoring(roomId)
        messagingTemplate.convertAndSend("/topic/monitoring/room/$roomId/snapshot", snapshot)
    }

    private fun persistAnswer(roomId: String, event: AnswerEventDto) {
        runCatching {
            val roomUuid = UUID.fromString(roomId)
            val studentUuid = UUID.fromString(event.studentId)
            answerService.submit(
                roomUuid,
                com.example.nunito.model.AnswerSubmission(
                    studentId = studentUuid,
                    gameId = runCatching { GameId.fromValue(event.gameId) }.getOrNull(),
                    questionId = event.questionId,
                    questionText = event.questionText,
                    answer = event.selectedOptionId ?: event.selectedOptionText ?: "",
                    isCorrect = event.isCorrect,
                    elapsedMs = event.elapsedMillis,
                    sentAt = event.answeredAt
                )
            )
        }.onFailure {
            // En un escenario real, se registraría el error; aquí no interrumpimos el broadcast
        }
    }
}
