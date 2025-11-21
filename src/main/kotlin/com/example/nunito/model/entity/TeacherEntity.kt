package com.example.nunito.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "teachers")
data class TeacherEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToOne(mappedBy = "teacher", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var settings: TeacherSettingsEntity? = null
)
