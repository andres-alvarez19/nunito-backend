package com.example.nunito.repository

import com.example.nunito.model.entity.AuthTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuthTokenRepository : JpaRepository<AuthTokenEntity, String> {
    fun deleteByExpiresAtBefore(now: Instant)
}
