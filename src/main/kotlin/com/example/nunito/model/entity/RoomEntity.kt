package com.example.nunito.model.entity

import com.example.nunito.model.Difficulty
import com.example.nunito.model.GameId
import com.example.nunito.model.RoomStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "rooms")
data class RoomEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false)
    var name: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_games", joinColumns = [JoinColumn(name = "room_id")])
    @Column(name = "game_id", nullable = false)
    @Enumerated(EnumType.STRING)
    val gameIds: MutableSet<GameId>,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var difficulty: Difficulty,

    @Column(name = "duration", nullable = false)
    var durationMinutes: Int,

    @Column(nullable = false)
    var isActive: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RoomStatus = RoomStatus.PENDING,

    @Column(nullable = true)
    var startsAt: Instant? = null,

    @Column(nullable = true)
    var endsAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    val teacher: TeacherEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id", nullable = false)
    var testSuite: TestSuiteEntity,

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "room_students",
        joinColumns = [JoinColumn(name = "room_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")]
    )
    val students: MutableSet<StudentEntity> = mutableSetOf(),

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    var updatedAt: Instant? = null
)
