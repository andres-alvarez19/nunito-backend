package com.example.nunito.api

import com.example.nunito.model.AnswerRecord
import com.example.nunito.model.AnswerSubmission
import com.example.nunito.service.AnswerService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms/{roomId}/answers")
class AnswerController(
    private val answerService: AnswerService
) {

    @PostMapping
    fun submitAnswer(
        @PathVariable roomId: UUID,
        @Valid @RequestBody submission: AnswerSubmission
    ): ResponseEntity<AnswerRecord> {
        val record = answerService.submit(roomId, submission)
        return ResponseEntity.status(HttpStatus.CREATED).body(record)
    }

    @GetMapping
    fun listAnswers(
        @PathVariable roomId: UUID,
        @RequestParam(required = false) studentId: UUID?,
        @RequestParam(required = false) questionId: String?
    ): List<AnswerRecord> = answerService.listAnswers(roomId, studentId, questionId)
}
