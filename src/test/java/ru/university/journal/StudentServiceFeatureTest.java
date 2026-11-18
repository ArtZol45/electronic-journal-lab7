
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceFeatureTest {

    private StudentRepository studentRepository;
    private GradeRepository gradeRepository;
    private AttendanceRepository attendanceRepository;
    private ScheduleRepository scheduleRepository;

    private StudentService service;

    private Student student;

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

        student = new Student(1, "Иванов Иван");

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));
    }

    @Test
    void shouldAddGrade() {
        when(gradeRepository.save(any(Grade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Grade result = service.addGrade(1, "Математика", 5);

        assertEquals(5, result.value());
        assertEquals("Математика", result.subject());
        assertEquals(1, result.studentId());
    }

    @Test
    void shouldRejectGradeForUnknownStudent() {
        when(studentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addGrade(100, "Математика", 5)
        );
    }

    @Test
    void shouldAddAttendance() {
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance present = service.addAttendance(
                1,
                LocalDate.of(2026, 11, 17),
                "Математика",
                true
        );

        assertTrue(present.present());

        Attendance absent = service.addAttendance(
                1,
                LocalDate.of(2026, 11, 18),
                "Физика",
                false
        );

        assertFalse(absent.present());
    }

    @Test
    void shouldRejectAttendanceForUnknownStudent() {
        when(studentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addAttendance(
                        100,
                        LocalDate.now(),
                        "Математика",
                        true
                )
        );
    }

    @Test
    void shouldReturnGradesByStudent() {
        List<Grade> grades = List.of(
                new Grade(student, "Математика", 5),
                new Grade(student, "Физика", 4)
        );

        when(gradeRepository.findByStudent_IdOrderByIdAsc(1L))
                .thenReturn(grades);

        List<Grade> result = service.getGradesByStudent(1);

        assertEquals(2, result.size());
        assertEquals(5, result.get(0).value());
    }

    @Test
    void shouldReturnAttendanceByStudent() {
        List<Attendance> records = List.of(
                new Attendance(
                        student,
                        LocalDate.of(2026, 11, 17),
                        "Математика",
                        true
                ),
                new Attendance(
                        student,
                        LocalDate.of(2026, 11, 18),
                        "Физика",
                        false
                )
        );

        when(attendanceRepository.findByStudent_IdOrderByDateAsc(1L))
                .thenReturn(records);

        List<Attendance> result =
                service.getAttendanceByStudent(1);

        assertEquals(2, result.size());
        assertTrue(result.get(0).present());
        assertFalse(result.get(1).present());
    }

    @Test
    void shouldReturnScheduleByDate() {
        LocalDate date = LocalDate.of(2026, 11, 17);

        List<ScheduleEntry> entries = List.of(
                new ScheduleEntry(
                        date,
                        LocalTime.of(9, 0),
                        "Математика",
                        "101"
                ),
                new ScheduleEntry(
                        date,
                        LocalTime.of(11, 0),
                        "Физика",
                        "202"
                )
        );

        when(scheduleRepository.findByDateOrderByTimeAsc(date))
                .thenReturn(entries);

        List<ScheduleEntry> result =
                service.getScheduleByDate(date);

        assertEquals(2, result.size());
        assertEquals(
                LocalTime.of(9, 0),
                result.get(0).getTime()
        );
    }

    @Test
    void shouldRejectNullScheduleDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getScheduleByDate(null)
        );

        verify(scheduleRepository, never())
                .findByDateOrderByTimeAsc(any());
    }

    @Test
    void shouldCreateStudentReport() {
        List<Grade> grades = List.of(
                new Grade(student, "Математика", 5),
                new Grade(student, "Физика", 4)
        );

        List<Attendance> records = List.of(
                new Attendance(
                        student,
                        LocalDate.of(2026, 11, 17),
                        "Математика",
                        true
                ),
                new Attendance(
                        student,
                        LocalDate.of(2026, 11, 18),
                        "Физика",
                        false
                )
        );

        when(gradeRepository.findByStudent_IdOrderByIdAsc(1L))
                .thenReturn(grades);

        when(attendanceRepository.findByStudent_IdOrderByDateAsc(1L))
                .thenReturn(records);

        StudentReport report = service.getStudentReport(1);

        assertEquals(1, report.studentId());
        assertEquals("Иванов Иван", report.fullName());
        assertEquals(2, report.gradesCount());
        assertEquals(4.5, report.averageGrade());
        assertEquals(2, report.attendanceCount());
        assertEquals(1, report.attendedCount());
        assertEquals(1, report.absencesCount());
    }

    @Test
    void shouldCreateEmptyStudentReport() {
        when(gradeRepository.findByStudent_IdOrderByIdAsc(1L))
                .thenReturn(List.of());

        when(attendanceRepository.findByStudent_IdOrderByDateAsc(1L))
                .thenReturn(List.of());

        StudentReport report = service.getStudentReport(1);

        assertEquals(0, report.gradesCount());
        assertEquals(0.0, report.averageGrade());
        assertEquals(0, report.attendanceCount());
        assertEquals(0, report.attendedCount());
        assertEquals(0, report.absencesCount());
    }

    @Test
    void shouldRejectReportForUnknownStudent() {
        when(studentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getStudentReport(100)
        );
    }
        @Test
    void shouldSaveScheduleGroupName() {
        ScheduleEntry entry = new ScheduleEntry(
                LocalDate.of(2026, 11, 18),
                LocalTime.of(10, 0),
                "Математика",
                "101",
                "ИС-21"
        );

        assertEquals("ИС-21", entry.getGroupName());
    }

    @Test
    void shouldRejectScheduleWithBlankGroupName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduleEntry(
                        LocalDate.of(2026, 11, 18),
                        LocalTime.of(10, 0),
                        "Математика",
                        "101",
                        " "
                )
        );
    }
}
