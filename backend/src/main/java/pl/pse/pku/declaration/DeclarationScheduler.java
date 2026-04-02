package pl.pse.pku.declaration;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.pse.pku.declarationtype.ScheduleEntryRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeclarationScheduler {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final DeclarationService declarationService;

    @Scheduled(cron = "0 0 1 * * *")
    public void generateDailyDeclarations() {
        int today = LocalDate.now().getDayOfMonth();
        log.info("Running daily declaration generation for day {}", today);

        var entries = scheduleEntryRepository.findByDay(today);
        if (entries.isEmpty()) {
            log.info("No schedule entries for day {}", today);
            return;
        }

        int totalCreated = 0;
        for (var entry : entries) {
            String code = entry.getDeclarationType().getCode();
            try {
                int created = declarationService.generateDeclarationsForSchedule(code, entry.getDay());
                totalCreated += created;
                if (created > 0) {
                    log.info("Generated {} declarations for type {} (day {})", created, code, entry.getDay());
                }
            } catch (Exception e) {
                log.error("Failed to generate declarations for type {} (day {}): {}", code, entry.getDay(), e.getMessage());
            }
        }
        log.info("Daily generation complete: {} declarations created", totalCreated);
    }
}
