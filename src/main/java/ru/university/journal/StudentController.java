
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Student addStudent(@RequestBody StudentRequest request) {
        return studentService.addStudent(
                request.id(),
                request.fullName(),
                request.groupName()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{studentId}")
    public Student updateStudent(
            @PathVariable long studentId,
            @RequestBody UpdateStudentRequest request
    ) {
        return studentService.updateStudent(
                studentId,
                request.fullName(),
                request.groupName()
        );
    }
    

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{studentId}")
    public Student deleteStudent(
            @PathVariable long studentId
    ) {
        return studentService.deactivateStudent(studentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @studentAccessService.canAccessStudent(authentication.name, #p0)")
    @GetMapping("/{studentId}/grades")
    public List<Grade> getGrades(@PathVariable long studentId) {
        return studentService.getGradesByStudent(studentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @studentAccessService.canAccessStudent(authentication.name, #p0)")
    @GetMapping("/{studentId}/attendance")
    public List<Attendance> getAttendance(
            @PathVariable long studentId
    ) {
        return studentService.getAttendanceByStudent(studentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/schedule")
    public ScheduleEntry addScheduleEntry(
            @RequestBody ScheduleRequest request
    ) {
        return studentService.addScheduleEntry(
                request.date(),
                request.time(),
                request.subject(),
                request.classroom(),
                request.groupName()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @GetMapping("/schedule")
    public List<ScheduleEntry> getSchedule(
            @RequestParam LocalDate date
    ) {
        return studentService.getScheduleByDate(date);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @studentAccessService.canAccessStudent(authentication.name, #p0)")
    @GetMapping("/{studentId}/report")
    public StudentReport getStudentReport(
            @PathVariable long studentId
    ) {
        return studentService.getStudentReport(studentId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return exception.getMessage();
    }

    public record StudentRequest(long id, String fullName, String groupName) {
    }

    public record UpdateStudentRequest(String fullName, String groupName) {
    }

    public record GradeRequest(String subject, int value) {
    }

    public record AttendanceRequest(
            LocalDate date,
            String subject,
            boolean present
    ) {
    }

    public record ScheduleRequest(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom,
            String groupName
    ) {
    }
}