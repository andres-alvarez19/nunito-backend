package com.example.nunito.service

import com.example.nunito.exception.NotFoundException
import com.example.nunito.model.Course
import com.example.nunito.model.CreateCourseRequest
import com.example.nunito.model.UpdateCourseRequest
import com.example.nunito.model.entity.CourseEntity
import com.example.nunito.repository.CourseRepository
import com.example.nunito.repository.TeacherRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val teacherRepository: TeacherRepository
) {

    @Transactional(readOnly = true)
    fun listCourses(teacherId: UUID): List<Course> {
        return courseRepository.findByTeacherId(teacherId).map { toModel(it) }
    }

    @Transactional
    fun createCourse(request: CreateCourseRequest): Course {
        val teacher = teacherRepository.findById(request.teacherId)
            .orElseThrow { NotFoundException("Profesor", request.teacherId.toString()) }

        val entity = CourseEntity(
            name = request.name,
            description = request.description,
            teacher = teacher
        )
        return toModel(courseRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getCourse(courseId: UUID): Course {
        val entity = courseRepository.findById(courseId)
            .orElseThrow { NotFoundException("Curso", courseId.toString()) }
        return toModel(entity)
    }

    @Transactional
    fun updateCourse(courseId: UUID, request: UpdateCourseRequest): Course {
        val entity = courseRepository.findById(courseId)
            .orElseThrow { NotFoundException("Curso", courseId.toString()) }

        request.name?.let { entity.name = it }
        request.description?.let { entity.description = it }

        return toModel(courseRepository.save(entity))
    }

    @Transactional
    fun deleteCourse(courseId: UUID) {
        if (!courseRepository.existsById(courseId)) {
            throw NotFoundException("Curso", courseId.toString())
        }
        courseRepository.deleteById(courseId)
    }

    private fun toModel(entity: CourseEntity): Course {
        return Course(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            teacherId = entity.teacher.id,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
