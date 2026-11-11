
package ru.university.journal;

import java.time.LocalDate;

public record Attendance(
        long studentId,
        LocalDate date,
        String subject,
        boolean present
) {
    public Attendance {
        if (studentId <= 0) {
            throw new IllegalArgumentException(
                    "ID студента должен быть положительным"
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

        subject = subject.trim();
    }
}
