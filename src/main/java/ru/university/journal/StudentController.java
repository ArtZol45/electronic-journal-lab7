
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PostMapping
    public Student addStudent(@RequestBody StudentRequest request) {
        return studentService.addStudent(
                request.id(),
                request.fullName()
        );
    }

    @PostMapping("/{studentId}/grades")
    public Grade addGrade(
            @PathVariable long studentId,
            @RequestBody GradeRequest request
    ) {
        return studentService.addGrade(
                studentId,
                request.subject(),
                request.value()
        );
    }

    @GetMapping("/{studentId}/grades")
    public List<Grade> getGrades(@PathVariable long studentId) {
        return studentService.getGradesByStudent(studentId);
    }

    public record StudentRequest(long id, String fullName) {
    }

    public record GradeRequest(String subject, int value) {
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(
            IllegalArgumentException.class
    )
    @org.springframework.web.bind.annotation.ResponseStatus(
            org.springframework.http.HttpStatus.BAD_REQUEST
    )
    public String handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return exception.getMessage();
    }


    @PostMapping("/{studentId}/attendance")
    public Attendance addAttendance(
            @PathVariable long studentId,
            @RequestBody AttendanceRequest request
    ) {
        return studentService.addAttendance(
                studentId,
                request.date(),
                request.subject(),
                request.present()
        );
    }

    @GetMapping("/{studentId}/attendance")
    public List<Attendance> getAttendance(
            @PathVariable long studentId
    ) {
        return studentService.getAttendanceByStudent(studentId);
    }

    public record AttendanceRequest(
            LocalDate date,
            String subject,
            boolean present
    ) {
    }

    @PostMapping("/schedule")
    public ScheduleEntry addScheduleEntry(
            @RequestBody ScheduleRequest request
    ) {
        return studentService.addScheduleEntry(
                request.date(),
                request.time(),
                request.subject(),
                request.classroom()
        );
    }

    @GetMapping("/schedule")
    public List<ScheduleEntry> getSchedule(
            @RequestParam LocalDate date
    ) {
        return studentService.getScheduleByDate(date);
    }

    public record ScheduleRequest(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom
    ) {
    }
    @GetMapping("/{studentId}/report")
    public StudentReport getStudentReport(
            @PathVariable long studentId
    ) {
        return studentService.getStudentReport(studentId);
    }

}
