package com.example.nunito.api

import com.example.nunito.model.RoomStatus
import com.example.nunito.model.RoomSummary
import com.example.nunito.model.TeacherMetrics
import com.example.nunito.model.TeacherSettings
import com.example.nunito.service.TeacherService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.example.nunito.model.RecentRoomSummary
import com.example.nunito.model.TeacherDashboardStats

@RestController
@RequestMapping("/api/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {

    @GetMapping("/{teacherId}/rooms")
    fun listRooms(
        @PathVariable teacherId: UUID,
        @RequestParam(required = false) status: String?
    ): List<RoomSummary> {
        val roomStatus = when (status?.lowercase()) {
            "active" -> RoomStatus.ACTIVE
            "pending" -> RoomStatus.PENDING
            "finished", "past" -> RoomStatus.FINISHED
            null -> null
            else -> RoomStatus.fromValue(status)
        }
        return teacherService.getTeacherRooms(teacherId, roomStatus)
    }

    @GetMapping("/{teacherId}/dashboard/stats")
    fun getDashboardStats(@PathVariable teacherId: UUID): TeacherDashboardStats =
        teacherService.getDashboardStats(teacherId)

    @GetMapping("/{teacherId}/dashboard/recent-rooms")
    fun getRecentRooms(@PathVariable teacherId: UUID): List<RecentRoomSummary> =
        teacherService.getRecentRooms(teacherId)

    @GetMapping("/{teacherId}/reports")
    fun listReports(@PathVariable teacherId: UUID) =
        teacherService.getReports(teacherId)

    @GetMapping("/{teacherId}/metrics")
    fun metrics(@PathVariable teacherId: UUID): TeacherMetrics =
        teacherService.getMetrics(teacherId)

    @GetMapping("/{teacherId}/settings")
    fun getSettings(@PathVariable teacherId: UUID): TeacherSettings =
        teacherService.getSettings(teacherId)

    @PutMapping("/{teacherId}/settings")
    fun updateSettings(
        @PathVariable teacherId: UUID,
        @Valid @RequestBody settings: TeacherSettings
    ): TeacherSettings = teacherService.updateSettings(teacherId, settings)
}
