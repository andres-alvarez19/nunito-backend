package com.example.nunito.api

import com.example.nunito.model.AnswerEventDto
import com.example.nunito.service.MonitoringService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class MonitoringWebSocketController(
    private val monitoringService: MonitoringService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/monitoring/room/{roomId}/answer")
    fun handleAnswer(
        @DestinationVariable roomId: String,
        @Payload event: AnswerEventDto
    ) {
        val snapshot = monitoringService.registerAnswer(roomId, event)
        messagingTemplate.convertAndSend("/topic/monitoring/room/$roomId/snapshot", snapshot)
    }

    @MessageMapping("/monitoring/room/{roomId}/reset")
    fun resetRoom(
        @DestinationVariable roomId: String
    ) {
        val snapshot = monitoringService.resetRoomMonitoring(roomId)
        messagingTemplate.convertAndSend("/topic/monitoring/room/$roomId/snapshot", snapshot)
    }
}
