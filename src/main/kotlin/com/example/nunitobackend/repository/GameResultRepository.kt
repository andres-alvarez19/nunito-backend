package com.example.nunitobackend.repository

import com.example.nunitobackend.model.GameResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GameResultRepository : JpaRepository<GameResult, UUID> {
    fun findByRoomId(roomId: UUID): List<GameResult>
}

