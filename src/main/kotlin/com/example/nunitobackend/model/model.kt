package com.example.nunitobackend.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "teachers")
data class Teacher(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String
)

// Nueva entidad Student
@Entity
@Table(name = "students")
data class Student(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String? = null
)

/**
 * difficulty is kept as String to match the frontend values: "easy", "medium", "hard".
 */
@Entity
@Table(name = "rooms")
data class Room(
    @Id
    val id: UUID = UUID.randomUUID(),
    val code: String,
    var name: String,
    var game: String,
    var difficulty: String,
    var duration: Int,
    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinColumn(name = "teacher_id")
    var teacher: Teacher,
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "room_students",
        joinColumns = [JoinColumn(name = "room_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")]
    )
    val students: MutableSet<Student> = mutableSetOf(),
    var isActive: Boolean = false
)

@Entity
@Table(name = "game_results")
data class GameResult(
    @Id
    val id: UUID = UUID.randomUUID(),
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val averageTime: Double,
    val score: Double,
    @ManyToOne
    @JoinColumn(name = "room_id")
    val room: Room
)
