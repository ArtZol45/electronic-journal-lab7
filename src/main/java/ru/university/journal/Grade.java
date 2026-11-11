
package ru.university.journal;

public record Grade(
        long studentId,
        String subject,
        int value
) {
    public Grade {
        if (studentId <= 0) {
            throw new IllegalArgumentException(
                    "ID студента должен быть положительным"
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

        subject = subject.trim();
    }
}
