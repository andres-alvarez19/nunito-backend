package com.example.nunito.repository

import com.example.nunito.model.entity.TestSuiteEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestSuiteRepository : JpaRepository<TestSuiteEntity, UUID> {
    fun findByCourseId(courseId: UUID): List<TestSuiteEntity>
}
