package com.example.nunito.repository

import com.example.nunito.model.entity.TeacherSettingsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeacherSettingsRepository : JpaRepository<TeacherSettingsEntity, UUID> {
    fun findByTeacherId(teacherId: UUID): TeacherSettingsEntity?
}
