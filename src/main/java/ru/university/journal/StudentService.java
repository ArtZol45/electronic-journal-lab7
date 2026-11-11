
package ru.university.journal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final List<Student> students = new ArrayList<>();
    private final List<Grade> grades = new ArrayList<>();

    private final List<Attendance> attendanceRecords = new ArrayList<>();
    private final List<ScheduleEntry> schedule = new ArrayList<>();

    public Student addStudent(long id, String fullName) {
        boolean exists = students.stream()
                .anyMatch(student -> student.getId() == id);

        if (exists) {
            throw new IllegalArgumentException(
                    "Студент с таким ID уже существует"
            );
        }

        Student student = new Student(id, fullName);
        students.add(student);
        return student;
    }

    public List<Student> getAllStudents() {
        return List.copyOf(students);
    }

    public Grade addGrade(long studentId, String subject, int value) {
        boolean studentExists = students.stream()
                .anyMatch(student -> student.getId() == studentId);

        if (!studentExists) {
            throw new IllegalArgumentException(
                    "Студент с таким ID не найден"
            );
        }

        Grade grade = new Grade(studentId, subject, value);
        grades.add(grade);
        return grade;
    }

    public List<Grade> getGradesByStudent(long studentId) {
        boolean studentExists = students.stream()
                .anyMatch(student -> student.getId() == studentId);

        if (!studentExists) {
            throw new IllegalArgumentException(
                    "Студент с таким ID не найден"
            );
        }

        return grades.stream()
                .filter(grade -> grade.studentId() == studentId)
                .toList();
    }

    public Attendance addAttendance(
            long studentId,
            java.time.LocalDate date,
            String subject,
            boolean present
    ) {
        boolean studentExists = students.stream()
                .anyMatch(student -> student.getId() == studentId);

        if (!studentExists) {
            throw new IllegalArgumentException(
                    "Студент с таким ID не найден"
            );
        }

        Attendance attendance = new Attendance(
                studentId, date, subject, present
        );

        attendanceRecords.add(attendance);
        return attendance;
    }

    public List<Attendance> getAttendanceByStudent(long studentId) {
        boolean studentExists = students.stream()
                .anyMatch(student -> student.getId() == studentId);

        if (!studentExists) {
            throw new IllegalArgumentException(
                    "Студент с таким ID не найден"
            );
        }

        return attendanceRecords.stream()
                .filter(record -> record.studentId() == studentId)
                .toList();
    }


    public ScheduleEntry addScheduleEntry(
            java.time.LocalDate date,
            java.time.LocalTime time,
            String subject,
            String classroom
    ) {
        ScheduleEntry entry = new ScheduleEntry(
                date, time, subject, classroom
        );

        schedule.add(entry);
        return entry;
    }

    public List<ScheduleEntry> getScheduleByDate(
            java.time.LocalDate date
    ) {
        if (date == null) {
            throw new IllegalArgumentException(
                    "Дата не должна быть пустой"
            );
        }

        return schedule.stream()
                .filter(entry -> entry.date().equals(date))
                .sorted(java.util.Comparator.comparing(
                        ScheduleEntry::time
                ))
                .toList();
    }


    public StudentReport getStudentReport(long studentId) {
        Student student = students.stream()
                .filter(item -> item.getId() == studentId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Студент с таким ID не найден"
                ));

        List<Grade> studentGrades = getGradesByStudent(studentId);
        List<Attendance> studentAttendance =
                getAttendanceByStudent(studentId);

        double averageGrade = studentGrades.stream()
                .mapToInt(Grade::value)
                .average()
                .orElse(0.0);

        int attendedCount = (int) studentAttendance.stream()
                .filter(Attendance::present)
                .count();

        int absencesCount = studentAttendance.size() - attendedCount;

        return new StudentReport(
                student.getId(),
                student.getFullName(),
                studentGrades.size(),
                averageGrade,
                studentAttendance.size(),
                attendedCount,
                absencesCount
        );
    }

}
