package com.example.nunitobackend.repository

import com.example.nunitobackend.model.Teacher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeacherRepository : JpaRepository<Teacher, UUID>

