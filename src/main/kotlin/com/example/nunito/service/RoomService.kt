package com.example.nunito.service

import com.example.nunito.exception.BadRequestException
import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.CreateRoomRequest
import com.example.nunito.model.GameId
import com.example.nunito.model.GameResultSubmission
import com.example.nunito.model.GameInfo
import com.example.nunito.model.Room
import com.example.nunito.model.RoomReport
import com.example.nunito.model.RoomReportSummary
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.RoomStatusUpdate
import com.example.nunito.model.RoomSummary
import com.example.nunito.model.RoomLiveStatus
import com.example.nunito.model.StudentJoinRequest
import com.example.nunito.model.StudentResult
import com.example.nunito.model.StudentSession
import com.example.nunito.model.StudentSummary
import com.example.nunito.model.Teacher
import com.example.nunito.model.RoomFullResults
import com.example.nunito.model.UpdateRoomRequest
import com.example.nunito.model.UserDto
import com.example.nunito.model.AnswerRecord
import com.example.nunito.model.entity.GameResultEntity
import com.example.nunito.model.entity.RoomEntity
import com.example.nunito.model.entity.StudentEntity
import com.example.nunito.repository.GameAnswerRepository
import com.example.nunito.repository.GameResultRepository
import com.example.nunito.repository.RoomRepository
import com.example.nunito.repository.StudentRepository
import com.example.nunito.repository.TeacherRepository
import com.example.nunito.repository.TestSuiteRepository
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

data class RoomState(
    val users: MutableSet<UserDto> = ConcurrentHashMap.newKeySet(),
    var status: RoomLiveStatus = RoomLiveStatus.WAITING
)

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val teacherRepository: TeacherRepository,
    private val studentRepository: StudentRepository,
    private val gameResultRepository: GameResultRepository,
    private val gameAnswerRepository: GameAnswerRepository,
    private val testSuiteRepository: TestSuiteRepository
) {

    private val logger = LoggerFactory.getLogger(RoomService::class.java)
    private val roomStates: MutableMap<String, RoomState> = ConcurrentHashMap()

    fun addUserToRoom(roomId: String, user: UserDto): List<UserDto> {
        val roomState = roomStates.computeIfAbsent(roomId) { RoomState() }
        roomState.users.removeIf { it.userId == user.userId }
        roomState.users.add(user)
        return roomState.users.sortedBy { it.name }
    }

    fun removeUserFromRoom(roomId: String, user: UserDto): List<UserDto> {
        val roomState = roomStates[roomId] ?: return emptyList()
        roomState.users.removeIf { it.userId == user.userId }
        return roomState.users.sortedBy { it.name }
    }

    fun getUsersInRoom(roomId: String): List<UserDto> {
        return roomStates[roomId]?.users?.sortedBy { it.name } ?: emptyList()
    }

    fun startRoom(roomId: String): RoomLiveStatus {
        val roomState = roomStates.computeIfAbsent(roomId) { RoomState() }
        roomState.status = RoomLiveStatus.STARTED
        return roomState.status
    }

    fun getRoomStatus(roomId: String): RoomLiveStatus {
        return roomStates[roomId]?.status ?: RoomLiveStatus.WAITING
    }

    @Transactional
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
        return rooms.map { toSummary(refreshRoomTiming(it)) }
    }

    @Transactional
    fun findRecentRooms(teacherId: UUID, limit: Int): List<RoomSummary> {
        val rooms = roomRepository.findByTeacherId(teacherId)
        return rooms.sortedByDescending { it.updatedAt ?: it.createdAt }
            .map { refreshRoomTiming(it) }
            .take(limit)
            .map { toSummary(it) }
    }

    @Transactional
    fun createRoom(request: CreateRoomRequest): Room {
        val durationMinutes = request.durationMinutes
            ?: throw BadRequestException("El campo duration o durationMinutes es obligatorio")
        if (request.games.isEmpty()) {
            throw BadRequestException("Debe enviar al menos un juego")
        }
        val teacher = teacherRepository.findById(request.teacherId)
            .orElseThrow { NotFoundException("Profesor", request.teacherId.toString()) }
        val testSuite = testSuiteRepository.findById(request.testSuiteId)
            .orElseThrow { NotFoundException("Conjunto de preguntas", request.testSuiteId.toString()) }

        val code = generateRoomCode()
        val roomEntity = RoomEntity(
            code = code,
            name = request.name,
            gameIds = request.games.toMutableSet(),
            difficulty = request.difficulty,
            durationMinutes = durationMinutes,
            teacher = teacher,
            testSuite = testSuite,
            status = RoomStatus.PENDING,
        )
        val savedRoom = roomRepository.save(roomEntity)
        logger.info("Sala creada: code=${savedRoom.code}, name=${savedRoom.name}, id=${savedRoom.id}")
        return toModel(savedRoom)
    }

    @Transactional
    fun getRoom(roomId: UUID): Room {
        val roomEntity = roomRepository.findById(roomId)
            .map { refreshRoomTiming(it) }
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        return toModel(roomEntity)
    }

    @Transactional
    fun getByCode(code: String): Room {
        val roomEntity = roomRepository.findByCode(code)
            ?.let { refreshRoomTiming(it) }
            ?: throw NotFoundException("Sala", code)
        return toModel(roomEntity)
    }

    @Transactional
    fun updateRoom(roomId: UUID, request: UpdateRoomRequest): Room {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }

        if (request.name != null) roomEntity.name = request.name
        if (request.gameIds != null) { // backward compatibility check
            if (request.gameIds.isEmpty()) {
                throw BadRequestException("Debe enviar al menos un juego")
            }
            roomEntity.gameIds.clear()
            roomEntity.gameIds.addAll(request.gameIds)
        }
        if (request.difficulty != null) roomEntity.difficulty = request.difficulty
        if (request.durationMinutes != null) roomEntity.durationMinutes = request.durationMinutes
        if (request.testSuiteId != null) {
            val testSuite = testSuiteRepository.findById(request.testSuiteId)
                .orElseThrow { NotFoundException("Conjunto de preguntas", request.testSuiteId.toString()) }
            roomEntity.testSuite = testSuite
        }
        if (request.isActive != null) roomEntity.isActive = request.isActive
        
        roomEntity.updatedAt = Instant.now()
        
        val savedRoom = roomRepository.save(roomEntity)
        return toModel(refreshRoomTiming(savedRoom))
    }

    @Transactional
    fun updateStatus(roomId: UUID, statusUpdate: RoomStatusUpdate): Room {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }

        roomEntity.status = statusUpdate.status
        when (statusUpdate.status) {
            RoomStatus.ACTIVE -> applyStartTiming(roomEntity, statusUpdate.startsAt, statusUpdate.endsAt)
            RoomStatus.FINISHED -> {
                roomEntity.isActive = false
                roomEntity.endsAt = statusUpdate.endsAt ?: roomEntity.endsAt ?: Instant.now()
            }
            else -> {
                if (statusUpdate.isActive != null) roomEntity.isActive = statusUpdate.isActive
                if (statusUpdate.startsAt != null) roomEntity.startsAt = statusUpdate.startsAt
                if (statusUpdate.endsAt != null) roomEntity.endsAt = statusUpdate.endsAt
            }
        }
        
        roomEntity.updatedAt = Instant.now()

        val savedRoom = roomRepository.save(roomEntity)
        return toModel(refreshRoomTiming(savedRoom))
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

    @Transactional
    fun listResults(roomId: UUID): List<StudentResult> {
        if (!roomRepository.existsById(roomId)) {
            throw NotFoundException("Sala", roomId.toString())
        }
        backfillResultsFromAnswers(roomId)
        val answersByKey = answersByStudentAndGame(roomId)
        val results = gameResultRepository.findByRoomId(roomId)
        return results.map {
            val key = Pair(it.student.id, it.gameId)
            val answers = answersByKey[key] ?: emptyList()
            toStudentResult(it, answers)
        }
    }

    @Transactional
    fun getResultsWithAnswers(roomId: UUID): RoomFullResults {
        if (!roomRepository.existsById(roomId)) {
            throw NotFoundException("Sala", roomId.toString())
        }
        backfillResultsFromAnswers(roomId)
        val answersByKey = answersByStudentAndGame(roomId)
        val results = gameResultRepository.findByRoomId(roomId)
        val students = results.map {
            val key = Pair(it.student.id, it.gameId)
            toStudentResult(it, answersByKey[key] ?: emptyList())
        }
        return RoomFullResults(roomId = roomId, students = students)
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

    @Transactional
    fun getRoomReport(roomId: UUID): RoomReport {
        val roomEntity = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        val refreshedRoom = refreshRoomTiming(roomEntity)
        backfillResultsFromAnswers(refreshedRoom.id)
        val answersByKey = answersByStudentAndGame(roomId)
        val results = gameResultRepository.findByRoomId(roomId)
        val resultsWithAnswers = results.map {
            val key = Pair(it.student.id, it.gameId)
            toStudentResult(it, answersByKey[key] ?: emptyList())
        }
        val summary = calculateSummary(refreshedRoom, results)

        return RoomReport(
            roomId = refreshedRoom.id,
            roomName = refreshedRoom.name,
            games = refreshedRoom.gameIds.map { GameInfo(it, it.label()) },
            difficulty = refreshedRoom.difficulty,
            studentsCount = summary.studentsCount,
            averageScore = summary.averageScore ?: 0.0,
            completionRate = summary.completionRate ?: 0.0,
            createdAt = refreshedRoom.createdAt,
            students = resultsWithAnswers
        )
    }

    @Transactional
    fun getReportSummariesForTeacher(teacherId: UUID): List<RoomReportSummary> {
        val rooms = roomRepository.findByTeacherId(teacherId)
        return rooms.map { room ->
            val refreshedRoom = refreshRoomTiming(room)
            val results = gameResultRepository.findByRoomId(refreshedRoom.id)
            val summary = calculateSummary(refreshedRoom, results)
            RoomReportSummary(
                roomId = refreshedRoom.id,
                roomName = refreshedRoom.name,
                games = refreshedRoom.gameIds.map { GameInfo(it, it.label()) },
                difficulty = refreshedRoom.difficulty,
                studentsCount = summary.studentsCount,
                averageScore = summary.averageScore ?: 0.0,
                completionRate = summary.completionRate ?: 0.0,
                createdAt = refreshedRoom.createdAt
            )
        }
    }

    private fun applyStartTiming(room: RoomEntity, startsAtOverride: Instant?, endsAtOverride: Instant?) {
        val now = Instant.now()
        val startTime = startsAtOverride ?: room.startsAt ?: now
        val calculatedEndsAt = startTime.plus(Duration.ofMinutes(room.durationMinutes.toLong()))

        room.startsAt = startTime
        room.endsAt = endsAtOverride ?: room.endsAt ?: calculatedEndsAt
        room.isActive = true
    }

    private fun refreshRoomTiming(room: RoomEntity): RoomEntity {
        val now = Instant.now()
        var dirty = false

        if (room.status == RoomStatus.ACTIVE) {
            if (room.startsAt == null) {
                room.startsAt = now
                dirty = true
            }
            if (room.endsAt == null && room.startsAt != null) {
                room.endsAt = room.startsAt!!.plus(Duration.ofMinutes(room.durationMinutes.toLong()))
                dirty = true
            }
            if (!room.isActive) {
                room.isActive = true
                dirty = true
            }
            val endsAt = room.endsAt
            if (endsAt != null && now.isAfter(endsAt)) {
                room.isActive = false
                room.status = RoomStatus.FINISHED
                dirty = true
            }
        } else if (room.status == RoomStatus.FINISHED && room.isActive) {
            room.isActive = false
            dirty = true
        }

        if (dirty) {
            room.updatedAt = now
            return roomRepository.save(room)
        }
        return room
    }

    private fun toModel(entity: RoomEntity): Room {
        return Room(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            games = entity.gameIds.map { GameInfo(it, it.label()) },
            difficulty = entity.difficulty,
            durationMinutes = entity.durationMinutes,
            isActive = entity.isActive,
            status = entity.status,
            testSuiteId = entity.testSuite.id!!,
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
            games = room.gameIds.map { GameInfo(it, it.label()) },
            difficulty = room.difficulty,
            durationMinutes = room.durationMinutes,
            isActive = room.isActive,
            status = room.status,
            testSuiteId = room.testSuite.id!!,
            studentsCount = studentsCount,
            averageScore = avgScore,
            completionRate = completionRate,
            createdAt = room.createdAt,
            lastActivityAt = room.updatedAt
        )
    }

    @Transactional(readOnly = true)
    private fun answersByStudentAndGame(roomId: UUID): Map<Pair<UUID, GameId>, List<AnswerRecord>> {
        val answers = gameAnswerRepository.findByRoomId(roomId)
            .filter { it.gameId != null }
        return answers.groupBy { Pair(it.student.id, it.gameId!!) }
            .mapValues { entry ->
                entry.value
                    .sortedWith(compareBy<com.example.nunito.model.entity.GameAnswerEntity> { it.createdAt }.thenBy { it.attempt })
                    .map { toAnswerRecord(it) }
            }
    }

    private fun toAnswerRecord(entity: com.example.nunito.model.entity.GameAnswerEntity): AnswerRecord {
        return AnswerRecord(
            id = entity.id!!,
            roomId = entity.room.id,
            studentId = entity.student.id,
            gameId = entity.gameId,
            questionId = entity.questionId,
            questionText = entity.questionText,
            answer = entity.answer,
            isCorrect = entity.isCorrect,
            elapsedMs = entity.elapsedMs,
            attempt = entity.attempt,
            createdAt = entity.createdAt,
            sentAt = entity.sentAt
        )
    }

    @Transactional
    protected fun backfillResultsFromAnswers(roomId: UUID) {
        val room = roomRepository.findById(roomId).orElse(null) ?: return
        val existingResults = gameResultRepository.findByRoomId(roomId)
        val existingKeys = existingResults.associateBy { Pair(it.student.id, it.gameId) }

        val answers = gameAnswerRepository.findByRoomId(roomId)
            .filter { it.gameId != null }
        if (answers.isEmpty()) return

        val grouped = answers.groupBy { Pair(it.student.id, it.gameId!!) }
        grouped.forEach { (key, studentAnswers) ->
            if (existingKeys.containsKey(key)) return@forEach
            val (studentId, gameId) = key
            val student = studentRepository.findById(studentId).orElse(null) ?: return@forEach
            val totalQuestions = studentAnswers.map { it.questionId }.distinct().size
            val correctAnswers = studentAnswers.count { it.isCorrect == true }
            val incorrectAnswers = (totalQuestions - correctAnswers).coerceAtLeast(0)
            val avgTimeSeconds = studentAnswers.mapNotNull { it.elapsedMs }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.div(1000.0)
                ?: 0.0
            val score = if (totalQuestions > 0) {
                (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100.0
            } else 0.0
            val completedAt = studentAnswers.maxOfOrNull { it.sentAt ?: it.createdAt } ?: Instant.now()

            val result = GameResultEntity(
                student = student,
                room = room,
                gameId = gameId,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                incorrectAnswers = incorrectAnswers,
                averageTimeSeconds = avgTimeSeconds,
                score = score,
                completedAt = completedAt
            )
            gameResultRepository.save(result)
        }
    }

    private fun toStudentResult(entity: GameResultEntity): StudentResult {
        return toStudentResult(entity, emptyList())
    }

    private fun toStudentResult(entity: GameResultEntity, answers: List<AnswerRecord>): StudentResult {
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
            completedAt = entity.completedAt,
            answers = answers
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
