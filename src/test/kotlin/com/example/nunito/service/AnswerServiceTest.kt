package com.example.nunito.service

import com.example.nunito.model.AnswerSubmission
import com.example.nunito.model.Difficulty
import com.example.nunito.model.GameId
import com.example.nunito.model.RoomStatus
import com.example.nunito.model.entity.CourseEntity
import com.example.nunito.model.entity.RoomEntity
import com.example.nunito.model.entity.StudentEntity
import com.example.nunito.model.entity.TeacherEntity
import com.example.nunito.model.entity.TestSuiteEntity
import com.example.nunito.repository.CourseRepository
import com.example.nunito.repository.GameAnswerRepository
import com.example.nunito.repository.RoomRepository
import com.example.nunito.repository.StudentRepository
import com.example.nunito.repository.TeacherRepository
import com.example.nunito.repository.TestSuiteRepository
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class AnswerServiceTest @Autowired constructor(
    private val answerService: AnswerService,
    private val roomRepository: RoomRepository,
    private val teacherRepository: TeacherRepository,
    private val testSuiteRepository: TestSuiteRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val gameAnswerRepository: GameAnswerRepository
) {

    private lateinit var room: RoomEntity
    private lateinit var student: StudentEntity

    @BeforeEach
    fun setup() {
        val teacher = teacherRepository.save(
            TeacherEntity(
                name = "Teacher",
                email = "teacher@example.com",
                passwordHash = "hash"
            )
        )
        val savedCourse = courseRepository.save(
            CourseEntity(
                name = "Course",
                description = "desc",
                teacher = teacher
            )
        )
        val testSuite = testSuiteRepository.save(
            TestSuiteEntity(
                name = "Suite",
                description = "desc",
                games = """["${GameId.IMAGE_WORD.value}"]""",
                course = savedCourse
            )
        )

        room = roomRepository.save(
            RoomEntity(
                code = "CODE-1",
                name = "Room 1",
                gameIds = mutableSetOf(GameId.IMAGE_WORD),
                difficulty = Difficulty.EASY,
                durationMinutes = 10,
                isActive = true,
                status = RoomStatus.ACTIVE,
                teacher = teacher,
                testSuite = testSuite
            )
        )

        student = studentRepository.save(StudentEntity(name = "Student A", email = "s@example.com"))
        room.students.add(student)
        roomRepository.save(room)
    }

    @Test
    fun `submit stores answer and returns record`() {
        val submission = AnswerSubmission(
            studentId = student.id,
            questionId = "q1",
            answer = "option-a",
            isCorrect = true,
            elapsedMs = 1200
        )

        val record = answerService.submit(room.id, submission)

        assertThat(record.id).isNotNull()
        assertThat(record.questionId).isEqualTo("q1")
        assertThat(record.answer).isEqualTo("option-a")
        assertThat(record.isCorrect).isTrue()
        assertThat(record.roomId).isEqualTo(room.id)
        assertThat(gameAnswerRepository.findByRoomId(room.id)).hasSize(1)
    }

    @Test
    fun `submit reuses existing answer unless replace is true`() {
        val first = answerService.submit(
            room.id,
            AnswerSubmission(
                studentId = student.id,
                questionId = "q1",
                answer = "first",
                isCorrect = false,
                attempt = 1
            )
        )

        val duplicate = answerService.submit(
            room.id,
            AnswerSubmission(
                studentId = student.id,
                questionId = "q1",
                answer = "ignored",
                isCorrect = true,
                attempt = 1
            )
        )

        val replaced = answerService.submit(
            room.id,
            AnswerSubmission(
                studentId = student.id,
                questionId = "q1",
                answer = "updated",
                isCorrect = true,
                attempt = 1,
                replace = true
            )
        )

        assertThat(duplicate.id).isEqualTo(first.id)
        assertThat(replaced.id).isEqualTo(first.id)
        assertThat(replaced.answer).isEqualTo("updated")
    }

    @Test
    fun `listAnswers filters by student`() {
        val otherStudent = studentRepository.save(StudentEntity(name = "Student B"))
        room.students.add(otherStudent)
        roomRepository.save(room)

        answerService.submit(
            room.id,
            AnswerSubmission(studentId = student.id, questionId = "q1", answer = "a1")
        )
        answerService.submit(
            room.id,
            AnswerSubmission(studentId = otherStudent.id, questionId = "q2", answer = "a2")
        )

        val records = answerService.listAnswers(room.id, studentId = student.id, questionId = null)

        assertThat(records).hasSize(1)
        assertThat(records.first().studentId).isEqualTo(student.id)
    }
}
