package com.example.nunito.api

import com.example.nunito.model.Course
import com.example.nunito.model.CreateCourseRequest
import com.example.nunito.model.UpdateCourseRequest
import com.example.nunito.service.CourseService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService
) {

    @GetMapping
    fun listCourses(@RequestParam teacherId: UUID): List<Course> =
        courseService.listCourses(teacherId)

    @PostMapping
    fun createCourse(@Valid @RequestBody request: CreateCourseRequest): ResponseEntity<Course> =
        ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request))

    @GetMapping("/{courseId}")
    fun getCourse(@PathVariable courseId: UUID): Course =
        courseService.getCourse(courseId)

    @PatchMapping("/{courseId}")
    fun updateCourse(
        @PathVariable courseId: UUID,
        @Valid @RequestBody request: UpdateCourseRequest
    ): Course = courseService.updateCourse(courseId, request)

    @DeleteMapping("/{courseId}")
    fun deleteCourse(@PathVariable courseId: UUID): ResponseEntity<Unit> {
        courseService.deleteCourse(courseId)
        return ResponseEntity.noContent().build()
    }
}
