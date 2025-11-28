package com.example.nunito.api

import com.example.nunito.model.UserDto
import com.example.nunito.model.RoomStatusDto
import com.example.nunito.service.RoomService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class RoomWebSocketController(
    private val roomService: RoomService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/room/{roomId}/join")
    fun handleJoin(
        @DestinationVariable roomId: String,
        @Payload user: UserDto
    ) {
        val usersInRoom = roomService.addUserToRoom(roomId, user)
        messagingTemplate.convertAndSend("/topic/room/$roomId/users", usersInRoom)
    }

    @MessageMapping("/room/{roomId}/leave")
    fun handleLeave(
        @DestinationVariable roomId: String,
        @Payload user: UserDto
    ) {
        val usersInRoom = roomService.removeUserFromRoom(roomId, user)
        messagingTemplate.convertAndSend("/topic/room/$roomId/users", usersInRoom)
    }

    @MessageMapping("/room/{roomId}/start")
    @SendTo("/topic/room/{roomId}/status")
    fun startRoom(
        @DestinationVariable roomId: String
    ): RoomStatusDto {
        val status = roomService.startRoom(roomId)
        return RoomStatusDto(roomId = roomId, status = status)
    }
}
