
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleEntry(
        LocalDate date,
        LocalTime time,
        String subject,
        String classroom
) {
    public ScheduleEntry {
        if (date == null) {
            throw new IllegalArgumentException(
                    "Дата занятия не должна быть пустой"
            );
        }

        if (time == null) {
            throw new IllegalArgumentException(
                    "Время занятия не должно быть пустым"
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

        subject = subject.trim();
        classroom = classroom.trim();
    }
}
