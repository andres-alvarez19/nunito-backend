package com.example.nunitobackend.controller

import com.example.nunitobackend.model.Room
import com.example.nunitobackend.model.GameResults
import com.example.nunitobackend.model.Student
import com.example.nunitobackend.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    fun getRoom(@PathVariable id: String): ResponseEntity<Any> {
        val r = roomService.getRoom(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @GetMapping("/code/{code}")
    fun getByCode(@PathVariable code: String): ResponseEntity<Any> {
        val r = roomService.findByCode(code) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PutMapping("/{id}/active")
    fun setActive(@PathVariable id: String, @RequestParam active: Boolean): ResponseEntity<Any> {
        val r = roomService.setActive(id, active) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PostMapping("/{id}/students")
    fun addStudent(@PathVariable id: String, @RequestBody student: Student): ResponseEntity<Any> {
        val r = roomService.addStudent(id, student) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(r)
    }

    @PostMapping("/{id}/results")
    fun addResults(@PathVariable id: String, @RequestBody results: GameResults): ResponseEntity<Any> {
        if (roomService.getRoom(id) == null) return ResponseEntity.notFound().build()
        roomService.addResults(id, results)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/results")
    fun getResults(@PathVariable id: String): ResponseEntity<Any> {
        if (roomService.getRoom(id) == null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(roomService.getResults(id))
    }
}
