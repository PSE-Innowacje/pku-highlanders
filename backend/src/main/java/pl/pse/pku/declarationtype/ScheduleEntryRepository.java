package pl.pse.pku.declarationtype;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    List<ScheduleEntry> findByDay(int day);
}
