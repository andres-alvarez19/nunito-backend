package com.example.nunito.repository

import com.example.nunito.model.entity.CourseEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<CourseEntity, UUID> {
    fun findByTeacherId(teacherId: UUID): List<CourseEntity>
}
