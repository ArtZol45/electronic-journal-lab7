
package ru.university.journal;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    private StudentRepository studentRepository;
    private GradeRepository gradeRepository;
    private AttendanceRepository attendanceRepository;
    private ScheduleRepository scheduleRepository;

    private StudentService service;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        gradeRepository = mock(GradeRepository.class);
        attendanceRepository = mock(AttendanceRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);

        service = new StudentService(
                studentRepository,
                gradeRepository,
                attendanceRepository,
                scheduleRepository
        );
    }

    @Test
    void shouldAddStudent() {
        Student student = new Student(1, "Иванов Иван");

        when(studentRepository.existsById(1L)).thenReturn(false);
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student result = service.addStudent(1, "Иванов Иван");

        assertEquals(1, result.getId());
        assertEquals("Иванов Иван", result.getFullName());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void shouldRejectDuplicateStudent() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addStudent(1, "Иванов Иван")
        );

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void shouldReturnAllStudents() {
        List<Student> students = List.of(
                new Student(1, "Иванов Иван"),
                new Student(2, "Петров Пётр")
        );

        when(studentRepository.findByActiveTrueOrderByIdAsc())
                .thenReturn(students);

        List<Student> result = service.getAllStudents();

        assertEquals(2, result.size());
        assertEquals("Иванов Иван", result.get(0).getFullName());

        verify(studentRepository).findByActiveTrueOrderByIdAsc();
    }

    @Test
    void shouldRejectInvalidStudentId() {
        when(studentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getGradesByStudent(100)
        );
    }
}
