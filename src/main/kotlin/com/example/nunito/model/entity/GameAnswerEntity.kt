package com.example.nunito.model.entity

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import com.example.nunito.model.GameId
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "game_answers",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_game_answers_room_student_question_attempt",
            columnNames = ["room_id", "student_id", "question_id", "attempt"]
        )
    ]
)
data class GameAnswerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: RoomEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "game_id")
    var gameId: GameId? = null,

    @Column(name = "question_id", nullable = false)
    val questionId: String,

    @Column(name = "question_text", columnDefinition = "TEXT")
    var questionText: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String,

    @Column(name = "is_correct")
    var isCorrect: Boolean? = null,

    @Column(name = "elapsed_ms")
    var elapsedMs: Long? = null,

    @Column(nullable = false)
    var attempt: Int = 1,

    @Column(name = "sent_at")
    var sentAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)
