package com.example.nunito.api

import com.example.nunito.model.CreateTestSuiteRequest
import com.example.nunito.model.TestSuite
import com.example.nunito.model.UpdateTestSuiteRequest
import com.example.nunito.service.TestSuiteService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TestSuiteController(
    private val testSuiteService: TestSuiteService
) {

    @GetMapping("/courses/{courseId}/test-suites")
    fun listTestSuites(@PathVariable courseId: UUID): List<TestSuite> =
        testSuiteService.listTestSuites(courseId)

    @PostMapping("/test-suites")
    fun createTestSuite(@Valid @RequestBody request: CreateTestSuiteRequest): ResponseEntity<TestSuite> =
        ResponseEntity.status(HttpStatus.CREATED).body(testSuiteService.createTestSuite(request))

    @GetMapping("/test-suites/{testSuiteId}")
    fun getTestSuite(@PathVariable testSuiteId: UUID): TestSuite =
        testSuiteService.getTestSuite(testSuiteId)

    @PatchMapping("/test-suites/{testSuiteId}")
    fun updateTestSuite(
        @PathVariable testSuiteId: UUID,
        @Valid @RequestBody request: UpdateTestSuiteRequest
    ): TestSuite = testSuiteService.updateTestSuite(testSuiteId, request)

    @DeleteMapping("/test-suites/{testSuiteId}")
    fun deleteTestSuite(@PathVariable testSuiteId: UUID): ResponseEntity<Unit> {
        testSuiteService.deleteTestSuite(testSuiteId)
        return ResponseEntity.noContent().build()
    }
}
