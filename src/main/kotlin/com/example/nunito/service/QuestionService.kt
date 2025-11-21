package com.example.nunito.service

import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.CreateQuestionRequest
import com.example.nunito.model.GameId
import com.example.nunito.model.Question
import com.example.nunito.model.entity.QuestionEntity
import com.example.nunito.repository.QuestionRepository
import com.example.nunito.repository.TestSuiteRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val testSuiteRepository: TestSuiteRepository
) {
    private val objectMapper = ObjectMapper()

    @Transactional(readOnly = true)
    fun listQuestions(testSuiteId: UUID, gameType: GameId?): List<Question> {
        val entities = if (gameType != null) {
            questionRepository.findByTestSuiteIdAndType(testSuiteId, gameType)
        } else {
            questionRepository.findByTestSuiteId(testSuiteId)
        }
        return entities.map { toModel(it) }
    }

    @Transactional
    fun createQuestion(request: CreateQuestionRequest): Question {
        val testSuite = testSuiteRepository.findById(request.testSuiteId)
            .orElseThrow { NotFoundException("Conjunto de preguntas", request.testSuiteId.toString()) }

        val optionsJson = request.options?.let { objectMapper.writeValueAsString(it) }

        val entity = QuestionEntity(
            text = request.text,
            type = request.type,
            options = optionsJson,
            correctAnswer = request.correctAnswer,
            testSuite = testSuite
        )
        return toModel(questionRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getQuestion(questionId: UUID): Question {
        val entity = questionRepository.findById(questionId)
            .orElseThrow { NotFoundException("Pregunta", questionId.toString()) }
        return toModel(entity)
    }

    @Transactional
    fun updateQuestion(questionId: UUID, request: CreateQuestionRequest): Question {
        val entity = questionRepository.findById(questionId)
            .orElseThrow { NotFoundException("Pregunta", questionId.toString()) }

        entity.text = request.text
        entity.type = request.type
        entity.options = request.options?.let { objectMapper.writeValueAsString(it) }
        entity.correctAnswer = request.correctAnswer

        return toModel(questionRepository.save(entity))
    }

    @Transactional
    fun deleteQuestion(questionId: UUID) {
        if (!questionRepository.existsById(questionId)) {
            throw NotFoundException("Pregunta", questionId.toString())
        }
        questionRepository.deleteById(questionId)
    }

    private fun toModel(entity: QuestionEntity): Question {
        val optionsMap = entity.options?.let {
            try {
                objectMapper.readValue(it, Map::class.java) as Map<String, Any>
            } catch (e: Exception) {
                null
            }
        }

        return Question(
            id = entity.id!!,
            text = entity.text,
            type = entity.type,
            options = optionsMap,
            correctAnswer = entity.correctAnswer,
            testSuiteId = entity.testSuite.id!!,
            createdAt = entity.createdAt
        )
    }
}
