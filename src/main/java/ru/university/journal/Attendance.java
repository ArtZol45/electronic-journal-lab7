
package ru.university.journal;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_records")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false)
    private boolean present;

    protected Attendance() {
    }

    public Attendance(
            Student student,
            LocalDate date,
            String subject,
            boolean present
    ) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "Студент не указан"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Дата занятия не должна быть пустой"
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Название дисциплины не должно быть пустым"
            );
        }

        this.student = student;
        this.date = date;
        this.subject = subject.trim();
        this.present = present;
    }

    public Long getId() {
        return id;
    }

    public long getStudentId() {
        return student.getId();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isPresent() {
        return present;
    }

    public long studentId() {
        return getStudentId();
    }

    public LocalDate date() {
        return getDate();
    }

    public String subject() {
        return getSubject();
    }

    public boolean present() {
        return isPresent();
    }
}
