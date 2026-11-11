package ru.university.journal;

public record StudentReport(
        long studentId,
        String fullName,
        int gradesCount,
        double averageGrade,
        int attendanceCount,
        int attendedCount,
        int absencesCount
) {
}
