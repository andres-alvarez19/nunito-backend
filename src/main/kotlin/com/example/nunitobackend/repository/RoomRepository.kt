package com.example.nunitobackend.repository

import com.example.nunitobackend.model.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RoomRepository : JpaRepository<Room, UUID> {
    fun findByCode(code: String): Room?
}

