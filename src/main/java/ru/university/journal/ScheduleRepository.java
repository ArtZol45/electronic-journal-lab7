package ru.university.journal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository
        extends JpaRepository<ScheduleEntry, Long> {

    List<ScheduleEntry> findByDateOrderByTimeAsc(LocalDate date);
}