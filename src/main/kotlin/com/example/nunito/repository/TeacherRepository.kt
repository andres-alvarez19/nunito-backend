package com.example.nunito.repository

import com.example.nunito.model.entity.TeacherEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeacherRepository : JpaRepository<TeacherEntity, UUID> {
    fun findByEmail(email: String): TeacherEntity?
    fun existsByEmail(email: String): Boolean
}
