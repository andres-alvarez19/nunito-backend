package com.example.nunito.api

import com.example.nunito.model.CreateQuestionRequest
import com.example.nunito.model.GameId
import com.example.nunito.model.Question
import com.example.nunito.service.QuestionService
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
@RequestMapping("/api")
class QuestionController(
    private val questionService: QuestionService
) {

    @GetMapping("/test-suites/{testSuiteId}/questions")
    fun listQuestions(
        @PathVariable testSuiteId: UUID,
        @RequestParam(required = false) gameType: GameId?
    ): List<Question> = questionService.listQuestions(testSuiteId, gameType)

    @PostMapping("/test-suites/{testSuiteId}/questions")
    fun createQuestion(
        @PathVariable testSuiteId: UUID,
        @Valid @RequestBody request: CreateQuestionRequest
    ): ResponseEntity<Question> {
        // Ensure the path variable matches the body if needed, or just use the body
        // Here we assume the body contains the correct testSuiteId or we could force it
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(request))
    }

    @GetMapping("/questions/{questionId}")
    fun getQuestion(@PathVariable questionId: UUID): Question =
        questionService.getQuestion(questionId)

    @PatchMapping("/questions/{questionId}")
    fun updateQuestion(
        @PathVariable questionId: UUID,
        @Valid @RequestBody request: CreateQuestionRequest
    ): Question = questionService.updateQuestion(questionId, request)

    @DeleteMapping("/questions/{questionId}")
    fun deleteQuestion(@PathVariable questionId: UUID): ResponseEntity<Unit> {
        questionService.deleteQuestion(questionId)
        return ResponseEntity.noContent().build()
    }
}
