package com.example.nunito.api

import com.example.nunito.model.CreateRoomRequest
import com.example.nunito.model.GameResultSubmission
import com.example.nunito.model.Room
import com.example.nunito.model.RoomFullResults
import com.example.nunito.model.RoomReport
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.RoomStatusUpdate
import com.example.nunito.model.RoomSummary
import com.example.nunito.model.StudentJoinRequest
import com.example.nunito.model.StudentResult
import com.example.nunito.model.StudentSession
import com.example.nunito.model.StudentSummary
import com.example.nunito.model.UpdateRoomRequest
import com.example.nunito.service.RoomService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService
) {

    @GetMapping
    fun listRooms(
        @RequestParam(required = false) teacherId: UUID?,
        @RequestParam(required = false) status: RoomStatus?
    ): List<RoomSummary> = roomService.listRooms(teacherId, status)

    @PostMapping
    fun createRoom(@Valid @RequestBody request: CreateRoomRequest): ResponseEntity<Room> =
        ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request))

    @GetMapping("/{roomId}")
    fun getRoom(@PathVariable roomId: UUID): Room =
        roomService.getRoom(roomId)

    @PatchMapping("/{roomId}")
    fun updateRoom(
        @PathVariable roomId: UUID,
        @Valid @RequestBody request: UpdateRoomRequest
    ): Room = roomService.updateRoom(roomId, request)

    @PatchMapping("/{roomId}/status")
    fun updateStatus(
        @PathVariable roomId: UUID,
        @Valid @RequestBody request: RoomStatusUpdate
    ): Room = roomService.updateStatus(roomId, request)

    @GetMapping("/code/{code}")
    fun getByCode(@PathVariable code: String): Room =
        roomService.getByCode(code)

    @PostMapping("/code/{code}/students")
    fun joinByCode(
        @PathVariable code: String,
        @Valid @RequestBody request: StudentJoinRequest
    ): StudentSession = roomService.addStudentByCode(code, request)

    @GetMapping("/{roomId}/students")
    fun listStudents(@PathVariable roomId: UUID): List<StudentSummary> =
        roomService.listStudents(roomId)

    @PostMapping("/{roomId}/students")
    fun addStudent(
        @PathVariable roomId: UUID,
        @Valid @RequestBody request: StudentJoinRequest
    ): ResponseEntity<StudentSummary> =
        ResponseEntity.status(HttpStatus.CREATED).body(roomService.addStudent(roomId, request))

    @GetMapping("/{roomId}/results")
    fun listResults(@PathVariable roomId: UUID): List<StudentResult> =
        roomService.listResults(roomId)

    @PostMapping("/{roomId}/results")
    fun addResult(
        @PathVariable roomId: UUID,
        @Valid @RequestBody submission: GameResultSubmission
    ): ResponseEntity<StudentResult> =
        ResponseEntity.status(HttpStatus.CREATED).body(roomService.addResult(roomId, submission))

    @GetMapping("/{roomId}/report")
    fun getReport(@PathVariable roomId: UUID): RoomReport =
        roomService.getRoomReport(roomId)

    @GetMapping("/{roomId}/results/full")
    fun getResultsWithAnswers(@PathVariable roomId: UUID): RoomFullResults =
        roomService.getResultsWithAnswers(roomId)
}
