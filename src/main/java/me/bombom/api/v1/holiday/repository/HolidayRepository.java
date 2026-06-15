package me.bombom.api.v1.holiday.repository;

import me.bombom.api.v1.holiday.domain.Holiday;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);

    boolean existsByDate(LocalDate date);

    boolean existsByDateAndIdNot(LocalDate date, Long id);
}
