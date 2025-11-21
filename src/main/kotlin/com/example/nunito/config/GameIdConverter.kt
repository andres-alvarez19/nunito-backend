package com.example.nunito.config

import com.example.nunito.model.GameId
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class GameIdConverter : Converter<String, GameId> {
    override fun convert(source: String): GameId? {
        return try {
            GameId.fromValue(source)
        } catch (e: Exception) {
            null
        }
    }
}
