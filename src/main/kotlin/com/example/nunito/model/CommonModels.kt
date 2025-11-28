package com.example.nunito.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class GameId(@get:JsonValue val value: String) {
    IMAGE_WORD("image-word"),
    SYLLABLE_COUNT("syllable-count"),
    RHYME_IDENTIFICATION("rhyme-identification"),
    AUDIO_RECOGNITION("audio-recognition");

    fun label(): String = when (this) {
        IMAGE_WORD -> "Asociación Imagen-Palabra"
        SYLLABLE_COUNT -> "Conteo de Sílabas"
        RHYME_IDENTIFICATION -> "Identificación de Rimas"
        AUDIO_RECOGNITION -> "Reconocimiento de Audio"
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): GameId =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Invalid gameId: $value")
    }
}

enum class Difficulty(@get:JsonValue val value: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): Difficulty =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Invalid difficulty: $value")
    }
}

enum class RoomStatus(@get:JsonValue val value: String) {
    PENDING("pending"),
    ACTIVE("active"),
    FINISHED("finished");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): RoomStatus =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Invalid room status: $value")
    }
}
