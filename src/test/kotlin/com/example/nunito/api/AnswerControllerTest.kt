package com.example.nunito.api

import com.example.nunito.model.AnswerRecord
import com.example.nunito.model.AnswerSubmission
import com.example.nunito.service.AnswerService
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AnswerController::class])
class AnswerControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockBean
    private lateinit var answerService: AnswerService

    @Test
    fun `POST answers delegates to service and returns created`() {
        val roomId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val record = AnswerRecord(
            id = UUID.randomUUID(),
            roomId = roomId,
            studentId = studentId,
            questionId = "q1",
            answer = "a1",
            isCorrect = true,
            elapsedMs = 100,
            attempt = 1,
            createdAt = Instant.now(),
            sentAt = null
        )
        whenever(answerService.submit(eq(roomId), any())).thenReturn(record)

        mockMvc.perform(
            post("/api/rooms/$roomId/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "studentId": "$studentId",
                        "questionId": "q1",
                        "answer": "a1",
                        "isCorrect": true,
                        "elapsedMs": 100
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.questionId").value("q1"))
            .andExpect(jsonPath("$.answer").value("a1"))
    }

    @Test
    fun `GET answers returns list from service`() {
        val roomId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val record = AnswerRecord(
            id = UUID.randomUUID(),
            roomId = roomId,
            studentId = studentId,
            questionId = "q1",
            answer = "a1",
            isCorrect = true,
            elapsedMs = 100,
            attempt = 1,
            createdAt = Instant.now(),
            sentAt = null
        )
        whenever(answerService.listAnswers(eq(roomId), eq(studentId), eq("q1")))
            .thenReturn(listOf(record))

        mockMvc.perform(get("/api/rooms/$roomId/answers?studentId=$studentId&questionId=q1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].studentId").value(studentId.toString()))
            .andExpect(jsonPath("$[0].questionId").value("q1"))
    }
}
