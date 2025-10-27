package com.example.nunitobackend.service

import com.example.nunitobackend.model.GameResult
import com.example.nunitobackend.model.Room
import com.example.nunitobackend.model.Student
import com.example.nunitobackend.repository.GameResultRepository
import com.example.nunitobackend.repository.RoomRepository
import com.example.nunitobackend.repository.StudentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val studentRepository: StudentRepository,
    private val gameResultRepository: GameResultRepository
) {

    fun createRoom(room: Room): Room {
        // teacher and students cascades will persist related entities
        return roomRepository.save(room)
    }

    fun getRoom(id: UUID): Room? = roomRepository.findById(id).orElse(null)

    fun listRooms(): List<Room> = roomRepository.findAll()

    fun findByCode(code: String): Room? = roomRepository.findByCode(code)

    fun setActive(id: UUID, active: Boolean): Room? {
        val r = getRoom(id) ?: return null
        r.isActive = active
        return roomRepository.save(r)
    }

    fun addStudent(id: UUID, student: Student): Room? {
        val r = getRoom(id) ?: return null
        // Save student first to ensure managed entity and id
        val savedStudent = studentRepository.save(student)
        r.students.add(savedStudent)
        return roomRepository.save(r)
    }

    fun addResults(roomId: UUID, res: GameResult) {
        val room = getRoom(roomId) ?: throw IllegalArgumentException("Room not found")
        val toSave = res.copy(room = room)
        gameResultRepository.save(toSave)
    }

    fun getResults(roomId: UUID): List<GameResult> = gameResultRepository.findByRoomId(roomId)
}
