package com.example.nunito.repository

import com.example.nunito.model.entity.GameResultEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GameResultRepository : JpaRepository<GameResultEntity, UUID> {
    fun findByRoomId(roomId: UUID): List<GameResultEntity>
    fun findByStudentId(studentId: UUID): List<GameResultEntity>
}
