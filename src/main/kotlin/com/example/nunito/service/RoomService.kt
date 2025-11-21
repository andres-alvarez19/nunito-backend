package com.example.nunito.service

import com.example.nunito.exception.BadRequestException
import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.CreateRoomRequest
import com.example.nunito.model.GameResultSubmission
import com.example.nunito.model.Room
import com.example.nunito.model.RoomReport
import com.example.nunito.model.RoomReportSummary
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.RoomStatusUpdate
import com.example.nunito.model.RoomSummary
import com.example.nunito.model.StudentJoinRequest
import com.example.nunito.model.StudentResult
import com.example.nunito.model.StudentSession
import com.example.nunito.model.StudentSummary
import com.example.nunito.model.Teacher
import com.example.nunito.model.UpdateRoomRequest
import com.example.nunito.model.entity.GameResultEntity
import com.example.nunito.model.entity.RoomEntity
import com.example.nunito.model.entity.StudentEntity
import com.example.nunito.repository.GameResultRepository
import com.example.nunito.repository.RoomRepository
import com.example.nunito.repository.StudentRepository
import com.example.nunito.repository.TeacherRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val teacherRepository: TeacherRepository,
    private val studentRepository: StudentRepository,
    private val gameResultRepository: GameResultRepository
) {

    @Transactional(readOnly = true)
    fun listRooms(teacherId: UUID?, status: RoomStatus?): List<RoomSummary> {
        val rooms = if (teacherId != null && status != null) {
            roomRepository.findByTeacherIdAndStatus(teacherId, status)
        } else if (teacherId != null) {
            roomRepository.findByTeacherId(teacherId)
        } else if (status != null) {
            roomRepository.findByStatus(status)
        } else {
            roomRepository.findAll()
        }
        return rooms.map { toSummary(it) }
    }

    @Transactional(readOnly = true)
    fun findRecentRooms(teacherId: UUID, limit: Int): List<RoomSummary> {
        val rooms = roomRepository.findByTeacherId(teacherId)
        return rooms.sortedByDescending { it.updatedAt ?: it.createdAt }
            .take(limit)
            .map { toSummary(it) }
    }

    @Transactional
    fun createRoom(request: CreateRoomRequest): Room {
        val teacher = teacherRepository.findById(request.teacherId)
            .orElseThrow { NotFoundException("Profesor", request.teacherId.toString()) }

        val code = generateRoomCode()
        val roomEntity = RoomEntity(
            code = code,
            name = request.name,
            gameId = request.gameId,
            difficulty = request.difficulty,
            durationMinutes = request.durationMinutes,
            teacher = teacher,
            status = RoomStatus.PENDING,
        )
        val savedRoom = roomRepository.save(roomEntity)
        return toModel(savedRoom)
    }

    @Transactional(readOnly = true)
    fun getRoom(roomId: UUID): Room {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        return toModel(roomEntity)
    }

    @Transactional(readOnly = true)
    fun getByCode(code: String): Room {
        val roomEntity = roomRepository.findByCode(code)
            ?: throw NotFoundException("Sala", code)
        return toModel(roomEntity)
    }

    @Transactional
    fun updateRoom(roomId: UUID, request: UpdateRoomRequest): Room {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }

        if (request.name != null) roomEntity.name = request.name
        if (request.gameId != null) roomEntity.gameId = request.gameId
        if (request.difficulty != null) roomEntity.difficulty = request.difficulty
        if (request.durationMinutes != null) roomEntity.durationMinutes = request.durationMinutes
        if (request.isActive != null) roomEntity.isActive = request.isActive
        
        roomEntity.updatedAt = Instant.now()
        
        val savedRoom = roomRepository.save(roomEntity)
        return toModel(savedRoom)
    }

    @Transactional
    fun updateStatus(roomId: UUID, statusUpdate: RoomStatusUpdate): Room {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }

        roomEntity.status = statusUpdate.status
        if (statusUpdate.isActive != null) roomEntity.isActive = statusUpdate.isActive
        if (statusUpdate.startsAt != null) roomEntity.startsAt = statusUpdate.startsAt
        if (statusUpdate.endsAt != null) roomEntity.endsAt = statusUpdate.endsAt
        
        roomEntity.updatedAt = Instant.now()

        val savedRoom = roomRepository.save(roomEntity)
        return toModel(savedRoom)
    }

    @Transactional(readOnly = true)
    fun listStudents(roomId: UUID): List<StudentSummary> {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        return roomEntity.students.map { 
            StudentSummary(id = it.id, name = it.name, email = it.email) 
        }
    }

    @Transactional
    fun addStudent(roomId: UUID, request: StudentJoinRequest): StudentSummary {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }

        // Check if student exists by email (if provided) or create new
        // For simplicity, we always create a new student record for the session or reuse if email matches?
        // The requirement says "StudentJoinRequest" has name and optional email.
        // If email is present, we could link to existing student.
        
        val studentEntity = if (!request.email.isNullOrBlank()) {
            studentRepository.findByEmail(request.email) ?: StudentEntity(
                name = request.name,
                email = request.email
            ).also { studentRepository.save(it) }
        } else {
            StudentEntity(name = request.name).also { studentRepository.save(it) }
        }

        roomEntity.students.add(studentEntity)
        roomEntity.updatedAt = Instant.now()
        roomRepository.save(roomEntity)

        return StudentSummary(id = studentEntity.id, name = studentEntity.name, email = studentEntity.email)
    }

    @Transactional
    fun addStudentByCode(code: String, request: StudentJoinRequest): StudentSession {
        val roomEntity = roomRepository.findByCode(code)
            ?: throw NotFoundException("Sala", code)
        
        val studentSummary = addStudent(roomEntity.id, request)
        val room = toModel(roomEntity) // Reload to get updated students if needed, but addStudent updates entity
        // We need to re-fetch room or manually add student to list for response
        // toModel fetches students from entity.students which is updated in memory by addStudent
        
        val sessionToken = "session-${room.id}-${studentSummary.id}"
        return StudentSession(room = room, student = studentSummary, sessionToken = sessionToken)
    }

    @Transactional(readOnly = true)
    fun listResults(roomId: UUID): List<StudentResult> {
        if (!roomRepository.existsById(roomId)) {
            throw NotFoundException("Sala", roomId.toString())
        }
        val results = gameResultRepository.findByRoomId(roomId)
        return results.map { toStudentResult(it) }
    }

    @Transactional
    fun addResult(roomId: UUID, submission: GameResultSubmission): StudentResult {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        
        if (submission.totalQuestions < submission.correctAnswers + submission.incorrectAnswers) {
            throw BadRequestException("Las respuestas correctas/incorrectas no pueden superar el total de preguntas")
        }

        val studentId = submission.studentId ?: throw BadRequestException("Student ID is required")
        val studentEntity = studentRepository.findById(studentId)
            .orElseThrow { NotFoundException("Estudiante", studentId.toString()) }

        val resultEntity = GameResultEntity(
            student = studentEntity,
            room = roomEntity,
            gameId = submission.gameId,
            totalQuestions = submission.totalQuestions,
            correctAnswers = submission.correctAnswers,
            incorrectAnswers = submission.incorrectAnswers,
            averageTimeSeconds = submission.averageTimeSeconds,
            score = submission.score,
            completedAt = submission.completedAt ?: Instant.now()
        )
        
        val savedResult = gameResultRepository.save(resultEntity)
        
        // Update room last activity
        roomEntity.updatedAt = Instant.now()
        roomRepository.save(roomEntity)

        return toStudentResult(savedResult)
    }

    @Transactional(readOnly = true)
    fun getRoomReport(roomId: UUID): RoomReport {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        
        val results = gameResultRepository.findByRoomId(roomId)
        val summary = calculateSummary(roomEntity, results)

        return RoomReport(
            roomId = roomEntity.id,
            roomName = roomEntity.name,
            gameId = roomEntity.gameId,
            difficulty = roomEntity.difficulty,
            studentsCount = summary.studentsCount,
            averageScore = summary.averageScore ?: 0.0,
            completionRate = summary.completionRate ?: 0.0,
            createdAt = roomEntity.createdAt,
            students = results.map { toStudentResult(it) }
        )
    }

    @Transactional(readOnly = true)
    fun getReportSummariesForTeacher(teacherId: UUID): List<RoomReportSummary> {
        val rooms = roomRepository.findByTeacherId(teacherId)
        return rooms.map { room ->
            val results = gameResultRepository.findByRoomId(room.id)
            val summary = calculateSummary(room, results)
            RoomReportSummary(
                roomId = room.id,
                roomName = room.name,
                gameId = room.gameId,
                difficulty = room.difficulty,
                studentsCount = summary.studentsCount,
                averageScore = summary.averageScore ?: 0.0,
                completionRate = summary.completionRate ?: 0.0,
                createdAt = room.createdAt
            )
        }
    }

    private fun toModel(entity: RoomEntity): Room {
        return Room(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            gameId = entity.gameId,
            difficulty = entity.difficulty,
            durationMinutes = entity.durationMinutes,
            isActive = entity.isActive,
            status = entity.status,
            startsAt = entity.startsAt,
            endsAt = entity.endsAt,
            teacherId = entity.teacher.id,
            teacher = Teacher(entity.teacher.id, entity.teacher.name, entity.teacher.email, entity.teacher.createdAt),
            students = entity.students.map { StudentSummary(it.id, it.name, it.email) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toSummary(entity: RoomEntity): RoomSummary {
        val results = gameResultRepository.findByRoomId(entity.id)
        return calculateSummary(entity, results)
    }

    private fun calculateSummary(room: RoomEntity, results: List<GameResultEntity>): RoomSummary {
        val studentsCount = room.students.size
        val avgScore = if (results.isNotEmpty()) results.map { it.score }.average() else null
        val completionRate = if (studentsCount > 0) {
            (results.distinctBy { it.student.id }.size.toDouble() / studentsCount.toDouble()).coerceAtMost(1.0) * 100
        } else null

        return RoomSummary(
            id = room.id,
            code = room.code,
            name = room.name,
            gameId = room.gameId,
            difficulty = room.difficulty,
            durationMinutes = room.durationMinutes,
            isActive = room.isActive,
            status = room.status,
            studentsCount = studentsCount,
            averageScore = avgScore,
            completionRate = completionRate,
            createdAt = room.createdAt,
            lastActivityAt = room.updatedAt
        )
    }

    private fun toStudentResult(entity: GameResultEntity): StudentResult {
        return StudentResult(
            id = entity.id,
            studentId = entity.student.id,
            studentName = entity.student.name,
            roomId = entity.room.id,
            gameId = entity.gameId,
            totalQuestions = entity.totalQuestions,
            correctAnswers = entity.correctAnswers,
            incorrectAnswers = entity.incorrectAnswers,
            averageTimeSeconds = entity.averageTimeSeconds,
            score = entity.score,
            completedAt = entity.completedAt
        )
    }

    private fun generateRoomCode(): String {
        var code: String
        do {
            code = UUID.randomUUID().toString().take(6).uppercase()
        } while (roomRepository.findByCode(code) != null)
        return code
    }
}
