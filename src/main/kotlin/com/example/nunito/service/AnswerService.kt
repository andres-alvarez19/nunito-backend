package com.example.nunito.service

import com.example.nunito.exception.BadRequestException
import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.AnswerRecord
import com.example.nunito.model.AnswerSubmission
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.entity.GameAnswerEntity
import com.example.nunito.repository.GameAnswerRepository
import com.example.nunito.repository.RoomRepository
import com.example.nunito.repository.StudentRepository
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnswerService(
    private val roomRepository: RoomRepository,
    private val studentRepository: StudentRepository,
    private val gameAnswerRepository: GameAnswerRepository
) {

    private val logger = LoggerFactory.getLogger(AnswerService::class.java)

    @Transactional
    fun submit(roomId: UUID, submission: AnswerSubmission): AnswerRecord {
        val room = roomRepository.findById(roomId)
            .orElseThrow { NotFoundException("Sala", roomId.toString()) }
        if (room.status != RoomStatus.ACTIVE || !room.isActive) {
            throw BadRequestException("La sala no está activa para recibir respuestas")
        }

        val student = studentRepository.findById(submission.studentId)
            .orElseThrow { NotFoundException("Estudiante", submission.studentId.toString()) }

        if (room.students.none { it.id == student.id }) {
            throw BadRequestException("El estudiante no está inscrito en la sala")
        }

        val attempt = submission.attempt
        val existing = gameAnswerRepository.findByRoomIdAndStudentIdAndQuestionIdAndAttempt(
            roomId = room.id,
            studentId = student.id,
            questionId = submission.questionId,
            attempt = attempt
        )

        if (existing != null) {
            if (submission.replace) {
                if (submission.gameId != null) {
                    existing.gameId = submission.gameId
                }
                if (submission.questionText != null) {
                    existing.questionText = submission.questionText
                }
                existing.answer = submission.answer
                existing.isCorrect = submission.isCorrect
                existing.elapsedMs = submission.elapsedMs
                existing.sentAt = submission.sentAt
                existing.updatedAt = Instant.now()
                val saved = gameAnswerRepository.save(existing)
                logger.info("Respuesta actualizada (replace=true) room={} student={} question={} attempt={}", room.id, student.id, submission.questionId, attempt)
                return toRecord(saved)
            }
            // Idempotencia: devolvemos la ya registrada
            return toRecord(existing)
        }

        val entity = GameAnswerEntity(
            room = room,
            student = student,
            gameId = submission.gameId,
            questionId = submission.questionId,
            questionText = submission.questionText,
            answer = submission.answer,
            isCorrect = submission.isCorrect,
            elapsedMs = submission.elapsedMs,
            attempt = attempt,
            sentAt = submission.sentAt
        )

        val saved = gameAnswerRepository.save(entity)
        logger.info("Respuesta guardada room={} student={} question={} attempt={}", room.id, student.id, submission.questionId, attempt)
        return toRecord(saved)
    }

    @Transactional(readOnly = true)
    fun listAnswers(roomId: UUID, studentId: UUID?, questionId: String?): List<AnswerRecord> {
        if (!roomRepository.existsById(roomId)) {
            throw NotFoundException("Sala", roomId.toString())
        }

        val answers = when {
            studentId != null && questionId != null -> gameAnswerRepository
                .findByRoomIdAndStudentId(roomId, studentId)
                .filter { it.questionId == questionId }
            studentId != null -> gameAnswerRepository.findByRoomIdAndStudentId(roomId, studentId)
            questionId != null -> gameAnswerRepository.findByRoomIdAndQuestionId(roomId, questionId)
            else -> gameAnswerRepository.findByRoomId(roomId)
        }

        return answers.sortedWith(compareBy<GameAnswerEntity> { it.createdAt }.thenBy { it.attempt })
            .map { toRecord(it) }
    }

    private fun toRecord(entity: GameAnswerEntity): AnswerRecord {
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
}
