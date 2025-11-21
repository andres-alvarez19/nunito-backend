package com.example.nunito.repository

import com.example.nunito.model.entity.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StudentRepository : JpaRepository<StudentEntity, UUID> {
    fun findByEmail(email: String): StudentEntity?
}
