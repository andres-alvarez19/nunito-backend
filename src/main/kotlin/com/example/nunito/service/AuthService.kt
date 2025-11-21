package com.example.nunito.service

import com.example.nunito.exception.ConflictException
import com.example.nunito.exception.UnauthorizedException
import com.example.nunito.model.AuthToken
import com.example.nunito.model.Teacher
import com.example.nunito.model.TeacherAuthResponse
import com.example.nunito.model.TeacherLoginRequest
import com.example.nunito.model.TeacherRegistrationRequest
import com.example.nunito.model.entity.AuthTokenEntity
import com.example.nunito.model.entity.TeacherEntity
import com.example.nunito.repository.AuthTokenRepository
import com.example.nunito.repository.TeacherRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val teacherRepository: TeacherRepository,
    private val authTokenRepository: AuthTokenRepository
) {

    @Transactional
    fun register(request: TeacherRegistrationRequest): TeacherAuthResponse {
        if (teacherRepository.existsByEmail(request.email)) {
            throw ConflictException("Ya existe un profesor registrado con ese email")
        }

        // TODO: Use a real password encoder
        val passwordHash = request.password 

        val teacherEntity = TeacherEntity(
            name = request.name,
            email = request.email,
            passwordHash = passwordHash
        )
        val savedTeacher = teacherRepository.save(teacherEntity)

        val token = issueToken(savedTeacher)
        return TeacherAuthResponse(teacher = mapToModel(savedTeacher), token = token)
    }

    @Transactional
    fun login(request: TeacherLoginRequest): TeacherAuthResponse {
        val teacherEntity = teacherRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Credenciales inválidas")
        
        // TODO: Use a real password encoder match
        if (teacherEntity.passwordHash != request.password) {
            throw UnauthorizedException("Credenciales inválidas")
        }

        val token = issueToken(teacherEntity)
        return TeacherAuthResponse(teacher = mapToModel(teacherEntity), token = token)
    }

    @Transactional(readOnly = true)
    fun getTeacherFromToken(authorization: String?): Teacher {
        if (authorization.isNullOrBlank()) throw UnauthorizedException("Debe enviar el token Bearer en Authorization")
        val parts = authorization.split(" ")
        val tokenValue = if (parts.size == 2 && parts[0].equals("Bearer", ignoreCase = true)) {
            parts[1]
        } else {
            authorization
        }

        val authToken = authTokenRepository.findById(tokenValue).orElseThrow {
            UnauthorizedException("Token inválido o expirado")
        }

        if (authToken.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Token expirado")
        }

        return mapToModel(authToken.teacher)
    }

    private fun issueToken(teacher: TeacherEntity): AuthToken {
        val tokenValue = "token-" + UUID.randomUUID().toString()
        val expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        
        val authTokenEntity = AuthTokenEntity(
            token = tokenValue,
            teacher = teacher,
            expiresAt = expiresAt
        )
        authTokenRepository.save(authTokenEntity)

        return AuthToken(
            accessToken = tokenValue,
            tokenType = "Bearer",
            expiresIn = 3600
        )
    }

    private fun mapToModel(entity: TeacherEntity): Teacher {
        return Teacher(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            createdAt = entity.createdAt
        )
    }
}
