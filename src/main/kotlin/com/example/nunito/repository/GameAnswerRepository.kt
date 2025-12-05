package com.example.nunito.repository

import com.example.nunito.model.entity.GameAnswerEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameAnswerRepository : JpaRepository<GameAnswerEntity, UUID> {
    fun findByRoomId(roomId: UUID): List<GameAnswerEntity>
    fun findByRoomIdAndStudentId(roomId: UUID, studentId: UUID): List<GameAnswerEntity>
    fun findByRoomIdAndQuestionId(roomId: UUID, questionId: String): List<GameAnswerEntity>
    fun findByRoomIdAndStudentIdAndQuestionIdAndAttempt(
        roomId: UUID,
        studentId: UUID,
        questionId: String,
        attempt: Int
    ): GameAnswerEntity?
}
