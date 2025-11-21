package com.example.nunito.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "students")
data class StudentEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    val email: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
