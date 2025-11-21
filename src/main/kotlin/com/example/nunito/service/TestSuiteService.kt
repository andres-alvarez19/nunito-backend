package com.example.nunito.service

import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.CreateTestSuiteRequest
import com.example.nunito.model.TestSuite
import com.example.nunito.model.UpdateTestSuiteRequest
import com.example.nunito.model.entity.TestSuiteEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.example.nunito.repository.CourseRepository
import com.example.nunito.repository.TestSuiteRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TestSuiteService(
    private val testSuiteRepository: TestSuiteRepository,
    private val courseRepository: CourseRepository
) {

    @Transactional(readOnly = true)
    fun listTestSuites(courseId: UUID): List<TestSuite> {
        return testSuiteRepository.findByCourseId(courseId).map { toModel(it) }
    }

    @Transactional
    fun createTestSuite(request: CreateTestSuiteRequest): TestSuite {
        val course = courseRepository.findById(request.courseId)
            .orElseThrow { NotFoundException("Curso", request.courseId.toString()) }

        val objectMapper = jacksonObjectMapper()
        val gamesJson = objectMapper.writeValueAsString(request.games)

        val entity = TestSuiteEntity(
            name = request.name,
            description = request.description,
            games = gamesJson,
            course = course
        )
        return toModel(testSuiteRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getTestSuite(testSuiteId: UUID): TestSuite {
        val entity = testSuiteRepository.findById(testSuiteId)
            .orElseThrow { NotFoundException("Conjunto de preguntas", testSuiteId.toString()) }
        return toModel(entity)
    }

    @Transactional
    fun updateTestSuite(testSuiteId: UUID, request: UpdateTestSuiteRequest): TestSuite {
        val entity = testSuiteRepository.findById(testSuiteId)
            .orElseThrow { NotFoundException("Conjunto de preguntas", testSuiteId.toString()) }

        request.name?.let { entity.name = it }
        request.description?.let { entity.description = it }
        request.games?.let {
            val objectMapper = jacksonObjectMapper()
            entity.games = objectMapper.writeValueAsString(it)
        }

        return toModel(testSuiteRepository.save(entity))
    }

    @Transactional
    fun deleteTestSuite(testSuiteId: UUID) {
        if (!testSuiteRepository.existsById(testSuiteId)) {
            throw NotFoundException("Conjunto de preguntas", testSuiteId.toString())
        }
        testSuiteRepository.deleteById(testSuiteId)
    }

    private fun toModel(entity: TestSuiteEntity): TestSuite {
        val objectMapper = jacksonObjectMapper()
        val games: List<String> = try {
            entity.games?.let { objectMapper.readValue(it, List::class.java) as List<String> } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        return TestSuite(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            courseId = entity.course.id!!,
            games = games,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
