
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_entries")
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 50)
    private String classroom;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;

    protected ScheduleEntry() {
    }

    public ScheduleEntry(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom,
            String groupName
    ) {
        if (date == null || time == null) {
            throw new IllegalArgumentException(
                    "Дата и время занятия обязательны"
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Название дисциплины не должно быть пустым"
            );
        }

        if (classroom == null || classroom.isBlank()) {
            throw new IllegalArgumentException(
                    "Аудитория не должна быть пустой"
            );
        }

        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(
                    "Учебная группа обязательна"
            );
        }

        this.date = date;
        this.time = time;
        this.subject = subject.trim();
        this.classroom = classroom.trim();
        this.groupName = groupName.trim();
    }

    // Старый конструктор для совместимости с существующими тестами
    public ScheduleEntry(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom
    ) {
        this(date, time, subject, classroom, "Не указана");
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getSubject() {
        return subject;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getGroupName() {
        return groupName;
    }
}
