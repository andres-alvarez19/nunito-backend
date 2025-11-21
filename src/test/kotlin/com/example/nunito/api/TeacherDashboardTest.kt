package com.example.nunito.api

import com.example.nunito.model.Difficulty
import com.example.nunito.model.GameId
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.Teacher
import com.example.nunito.service.TeacherService
import com.example.nunito.model.TeacherDashboardStats
import com.example.nunito.model.RecentRoomSummary
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(TeacherController::class)
class TeacherDashboardTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var teacherService: TeacherService

    @Test
    fun `getDashboardStats returns stats`() {
        val teacherId = UUID.randomUUID()
        val stats = TeacherDashboardStats(
            activeRoomsCount = 3,
            connectedStudentsCount = 45,
            completedActivitiesCount = 12,
            averageProgress = 78
        )

        `when`(teacherService.getDashboardStats(teacherId)).thenReturn(stats)

        mockMvc.perform(get("/api/teachers/$teacherId/dashboard/stats")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activeRoomsCount").value(3))
            .andExpect(jsonPath("$.connectedStudentsCount").value(45))
            .andExpect(jsonPath("$.completedActivitiesCount").value(12))
            .andExpect(jsonPath("$.averageProgress").value(78))
    }

    @Test
    fun `getRecentRooms returns list of rooms`() {
        val teacherId = UUID.randomUUID()
        val room = RecentRoomSummary(
            id = UUID.randomUUID(),
            title = "Clase 3°A - Fonología",
            gameLabel = "Asociación Imagen-Palabra",
            difficulty = "easy",
            createdAt = Instant.parse("2024-01-15T10:30:00Z"),
            students = 8,
            average = 85,
            status = "active"
        )

        `when`(teacherService.getRecentRooms(teacherId)).thenReturn(listOf(room))

        mockMvc.perform(get("/api/teachers/$teacherId/dashboard/recent-rooms")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].title").value("Clase 3°A - Fonología"))
            .andExpect(jsonPath("$[0].gameLabel").value("Asociación Imagen-Palabra"))
            .andExpect(jsonPath("$[0].status").value("active"))
    }

    @Test
    fun `listRooms with status past maps to FINISHED`() {
        val teacherId = UUID.randomUUID()
        
        `when`(teacherService.getTeacherRooms(teacherId, RoomStatus.FINISHED)).thenReturn(emptyList())

        mockMvc.perform(get("/api/teachers/$teacherId/rooms")
            .param("status", "past")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            
        // Verify service was called with FINISHED
        org.mockito.Mockito.verify(teacherService).getTeacherRooms(teacherId, RoomStatus.FINISHED)
    }
}
