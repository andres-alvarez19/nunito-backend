package com.example.nunito.model.entity

import com.example.nunito.model.GameId
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "game_results")
data class GameResultEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: RoomEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gameId: GameId,

    @Column(nullable = false)
    val totalQuestions: Int,

    @Column(nullable = false)
    val correctAnswers: Int,

    @Column(nullable = false)
    val incorrectAnswers: Int,

    @Column(nullable = false)
    val averageTimeSeconds: Double,

    @Column(nullable = false)
    val score: Double,

    @Column(nullable = false)
    val completedAt: Instant = Instant.now()
)
