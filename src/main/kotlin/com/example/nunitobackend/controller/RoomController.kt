package com.example.nunitobackend.controller

import com.example.nunitobackend.model.GameResult
import com.example.nunitobackend.model.Room
import com.example.nunitobackend.model.Student
import com.example.nunitobackend.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/rooms")
class RoomController(private val roomService: RoomService) {

    @PostMapping
    fun createRoom(@RequestBody room: Room): ResponseEntity<Room> {
        val created = roomService.createRoom(room)
        return ResponseEntity.ok(created)
    }

    @GetMapping
    fun listRooms() = ResponseEntity.ok(roomService.listRooms())

    @GetMapping("/{id}")
    fun getRoom(@PathVariable id: UUID): ResponseEntity<Any> {
        val r = roomService.getRoom(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @GetMapping("/code/{code}")
    fun getByCode(@PathVariable code: String): ResponseEntity<Any> {
        val r = roomService.findByCode(code) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PutMapping("/{id}/active")
    fun setActive(@PathVariable id: UUID, @RequestParam active: Boolean): ResponseEntity<Any> {
        val r = roomService.setActive(id, active) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PostMapping("/{id}/students")
    fun addStudent(@PathVariable id: UUID, @RequestBody student: Student): ResponseEntity<Any> {
        val r = roomService.addStudent(id, student) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PostMapping("/{id}/results")
    fun addResults(@PathVariable id: UUID, @RequestBody results: GameResult): ResponseEntity<Any> {
        try {
            roomService.addResults(id, results)
            return ResponseEntity.ok().build()
        } catch (_: IllegalArgumentException) {
            return ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/results")
    fun getResults(@PathVariable id: UUID): ResponseEntity<Any> {
        if (roomService.getRoom(id) == null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(roomService.getResults(id))
    }
}
