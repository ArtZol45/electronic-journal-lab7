
package ru.university.journal;

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
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(name = "grade_value", nullable = false)
    private int value;

    protected Grade() {
    }

    public Grade(Student student, String subject, int value) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "Студент не указан"
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Название дисциплины не должно быть пустым"
            );
        }

        if (value < 2 || value > 5) {
            throw new IllegalArgumentException(
                    "Оценка должна быть от 2 до 5"
            );
        }

        this.student = student;
        this.subject = subject.trim();
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public long getStudentId() {
        return student.getId();
    }

    public String getSubject() {
        return subject;
    }

    public int getValue() {
        return value;
    }

    public long studentId() {
        return getStudentId();
    }

    public String subject() {
        return getSubject();
    }

    public int value() {
        return getValue();
    }
}
