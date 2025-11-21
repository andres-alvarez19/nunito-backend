package com.example.nunito.model.entity

import com.example.nunito.model.Difficulty
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "teacher_settings")
data class TeacherSettingsEntity(
    @Id
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    val teacher: TeacherEntity,

    @ElementCollection
    @CollectionTable(name = "teacher_available_games", joinColumns = [JoinColumn(name = "teacher_settings_id")])
    @MapKeyColumn(name = "game_id")
    @Column(name = "is_enabled")
    var availableGames: MutableMap<String, Boolean> = mutableMapOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var defaultDifficulty: Difficulty,

    @Column(nullable = false)
    var questionTimeSeconds: Int,

    @Column(nullable = false)
    var feedbackSounds: Boolean,

    @Column(nullable = false)
    var feedbackAnimations: Boolean,

    @Column(nullable = false)
    var feedbackHints: Boolean
)
