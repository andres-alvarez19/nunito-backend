package com.example.nunito.service

import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.Difficulty
import com.example.nunito.model.RecentRoomSummary
import com.example.nunito.model.RoomReportSummary
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.RoomSummary
import com.example.nunito.model.Teacher
import com.example.nunito.model.TeacherDashboardStats
import com.example.nunito.model.TeacherMetrics
import com.example.nunito.model.TeacherSettings
import com.example.nunito.model.TeacherSettingsFeedback
import com.example.nunito.model.entity.TeacherSettingsEntity
import com.example.nunito.repository.TeacherRepository
import com.example.nunito.repository.TeacherSettingsRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeacherService(
    private val teacherRepository: TeacherRepository,
    private val teacherSettingsRepository: TeacherSettingsRepository,
    private val roomService: RoomService
) {

    @Transactional(readOnly = true)
    fun getTeacher(teacherId: UUID): Teacher {
        val entity = teacherRepository.findById(teacherId)
            .orElseThrow { NotFoundException("Profesor", teacherId.toString()) }
        return Teacher(entity.id, entity.name, entity.email, entity.createdAt)
    }

    fun getTeacherRooms(teacherId: UUID, status: RoomStatus?): List<RoomSummary> =
        roomService.listRooms(teacherId, status)

    fun getReports(teacherId: UUID): List<RoomReportSummary> =
        roomService.getReportSummariesForTeacher(teacherId)

    fun getDashboardStats(teacherId: UUID): TeacherDashboardStats {
        val metrics = getMetrics(teacherId)
        return TeacherDashboardStats(
            activeRoomsCount = metrics.activeRooms,
            connectedStudentsCount = metrics.connectedStudents,
            completedActivitiesCount = metrics.completedActivities,
            averageProgress = metrics.averageScore.toInt()
        )
    }

    fun getRecentRooms(teacherId: UUID): List<RecentRoomSummary> {
        val rooms = roomService.findRecentRooms(teacherId, 5)
        return rooms.map { room ->
            RecentRoomSummary(
                id = room.id,
                title = room.name,
                gameLabel = getGameLabel(room.gameId),
                difficulty = room.difficulty.value,
                createdAt = room.createdAt,
                students = room.studentsCount,
                average = room.averageScore?.toInt() ?: 0,
                status = room.status.value
            )
        }
    }

    private fun getGameLabel(gameId: com.example.nunito.model.GameId): String {
        return when (gameId) {
            com.example.nunito.model.GameId.IMAGE_WORD -> "Asociación Imagen-Palabra"
            com.example.nunito.model.GameId.SYLLABLE_COUNT -> "Conteo de Sílabas"
            com.example.nunito.model.GameId.RHYME_IDENTIFICATION -> "Identificación de Rimas"
            com.example.nunito.model.GameId.AUDIO_RECOGNITION -> "Reconocimiento de Audio"
        }
    }

    @Transactional(readOnly = true)
    fun getMetrics(teacherId: UUID): TeacherMetrics {
        if (!teacherRepository.existsById(teacherId)) {
            throw NotFoundException("Profesor", teacherId.toString())
        }
        
        val rooms = roomService.listRooms(teacherId, null)
        val activeRooms = rooms.count { it.status == RoomStatus.ACTIVE }
        
        val connectedStudents = rooms.sumOf { it.studentsCount }
        
        var completedActivities = 0
        var totalScore = 0.0
        var scoreCount = 0
        
        // We can use roomService.getReportSummariesForTeacher(teacherId) but it doesn't give result count.
        // Let's use roomService.listRooms(teacherId, null) which returns summaries.
        // Summaries are calculated from results.
        // But we need the raw count.
        
        // Let's just iterate over rooms and get their reports.
        // This is inefficient but works.
        val roomReports = rooms.map { roomService.getRoomReport(it.id) }
        
        roomReports.forEach { report ->
            completedActivities += report.students.size // Each student result is an activity completion? 
            // Wait, StudentResult is one game session.
            // Yes.
            report.students.forEach { result ->
                totalScore += result.score
                scoreCount++
            }
        }
        
        val averageScore = if (scoreCount > 0) totalScore / scoreCount else 0.0

        return TeacherMetrics(
            activeRooms = activeRooms,
            connectedStudents = connectedStudents,
            completedActivities = completedActivities,
            averageScore = averageScore
        )
    }

    @Transactional
    fun getSettings(teacherId: UUID): TeacherSettings {
        val teacherEntity = teacherRepository.findById(teacherId)
            .orElseThrow { NotFoundException("Profesor", teacherId.toString()) }
        
        val settingsEntity = teacherEntity.settings ?: createDefaultSettings(teacherEntity)
        
        return mapSettingsToModel(settingsEntity)
    }

    @Transactional
    fun updateSettings(teacherId: UUID, settings: TeacherSettings): TeacherSettings {
        val teacherEntity = teacherRepository.findById(teacherId)
            .orElseThrow { NotFoundException("Profesor", teacherId.toString()) }
        
        var settingsEntity = teacherEntity.settings
        if (settingsEntity == null) {
            settingsEntity = createDefaultSettings(teacherEntity)
        }
        
        settingsEntity.availableGames = settings.availableGames.toMutableMap()
        settingsEntity.defaultDifficulty = settings.defaultDifficulty
        settingsEntity.questionTimeSeconds = settings.questionTimeSeconds
        settingsEntity.feedbackSounds = settings.feedback.sounds
        settingsEntity.feedbackAnimations = settings.feedback.animations
        settingsEntity.feedbackHints = settings.feedback.hints
        
        teacherSettingsRepository.save(settingsEntity)
        teacherEntity.settings = settingsEntity
        teacherRepository.save(teacherEntity)
        
        return mapSettingsToModel(settingsEntity)
    }

    private fun createDefaultSettings(teacher: com.example.nunito.model.entity.TeacherEntity): TeacherSettingsEntity {
        val settings = TeacherSettingsEntity(
            teacher = teacher,
            availableGames = com.example.nunito.model.GameId.entries.associate { it.name to true }.toMutableMap(), // Using name as key for map
            defaultDifficulty = Difficulty.EASY,
            questionTimeSeconds = 30,
            feedbackSounds = true,
            feedbackAnimations = true,
            feedbackHints = true
        )
        // We need to save it? Cascade should handle it if we add to teacher.
        // But we are in a transaction.
        return settings
    }

    private fun mapSettingsToModel(entity: TeacherSettingsEntity): TeacherSettings {
        return TeacherSettings(
            availableGames = entity.availableGames,
            defaultDifficulty = entity.defaultDifficulty,
            questionTimeSeconds = entity.questionTimeSeconds,
            feedback = TeacherSettingsFeedback(
                sounds = entity.feedbackSounds,
                animations = entity.feedbackAnimations,
                hints = entity.feedbackHints
            )
        )
    }
}
