package com.example.nunitobackend.model

data class Teacher(
    val name: String,
    val email: String
)

// Nueva entidad Student
data class Student(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String? = null
)

/**
 * difficulty is kept as String to match the frontend values: "easy", "medium", "hard".
 * This keeps JSON mapping simple without custom serializers.
 */
data class Room(
    val id: String = java.util.UUID.randomUUID().toString(),
    val code: String,
    var name: String,
    var game: String,
    var difficulty: String,
    var duration: Int,
    val teacher: Teacher,
    val students: MutableList<Student> = mutableListOf(),
    var isActive: Boolean = false
)

data class GameResults(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val averageTime: Double,
    val score: Double
)
