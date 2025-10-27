package com.example.nunitobackend.service

import com.example.nunitobackend.model.Room
import com.example.nunitobackend.model.GameResults
import com.example.nunitobackend.model.Student
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RoomService {
    private val rooms: ConcurrentHashMap<String, Room> = ConcurrentHashMap()

    fun createRoom(room: Room): Room {
        rooms[room.id] = room
        return room
    }

    fun getRoom(id: String): Room? = rooms[id]

    fun listRooms(): List<Room> = rooms.values.toList()

    fun findByCode(code: String): Room? = rooms.values.find { it.code == code }

    fun setActive(id: String, active: Boolean): Room? {
        val r = rooms[id] ?: return null
        r.isActive = active
        return r
    }

    fun addStudent(id: String, student: Student): Room? {
        val r = rooms[id] ?: return null
        r.students.add(student)
        return r
    }

    // For demo: store results in-memory map keyed by room id
    private val results: ConcurrentHashMap<String, MutableList<GameResults>> = ConcurrentHashMap()

    fun addResults(roomId: String, res: GameResults) {
        results.computeIfAbsent(roomId) { mutableListOf() }.add(res)
    }

    fun getResults(roomId: String): List<GameResults> = results[roomId]?.toList() ?: emptyList()
}
