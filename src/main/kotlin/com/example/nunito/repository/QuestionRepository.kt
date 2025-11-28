package com.example.nunito.repository

import com.example.nunito.model.GameId
import com.example.nunito.model.entity.QuestionEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<QuestionEntity, UUID> {
    fun findByTestSuiteId(testSuiteId: UUID): List<QuestionEntity>
    fun findByTestSuiteIdAndType(testSuiteId: UUID, type: GameId): List<QuestionEntity>
    fun deleteByTestSuiteId(testSuiteId: UUID)
}
