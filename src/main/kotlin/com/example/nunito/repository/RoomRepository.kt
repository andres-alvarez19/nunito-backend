package com.example.nunito.repository

import com.example.nunito.model.RoomStatus
import com.example.nunito.model.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RoomRepository : JpaRepository<RoomEntity, UUID> {
    fun findByCode(code: String): RoomEntity?
    fun findByTeacherId(teacherId: UUID): List<RoomEntity>
    fun findByTeacherIdAndStatus(teacherId: UUID, status: RoomStatus): List<RoomEntity>
    fun findByStatus(status: RoomStatus): List<RoomEntity>
}
