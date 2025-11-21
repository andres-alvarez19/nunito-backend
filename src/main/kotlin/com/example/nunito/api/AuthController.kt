package com.example.nunito.api

import com.example.nunito.model.Teacher
import com.example.nunito.model.TeacherAuthResponse
import com.example.nunito.model.TeacherLoginRequest
import com.example.nunito.model.TeacherRegistrationRequest
import com.example.nunito.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: TeacherRegistrationRequest): ResponseEntity<TeacherAuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: TeacherLoginRequest): TeacherAuthResponse =
        authService.login(request)

    @GetMapping("/me")
    fun me(@RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?): Teacher =
        authService.getTeacherFromToken(authorization)
}
