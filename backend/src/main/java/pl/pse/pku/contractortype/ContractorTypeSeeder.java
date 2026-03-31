package pl.pse.pku.contractortype;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractorTypeSeeder implements ApplicationRunner {

    private final ContractorTypeRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        List.of(
            new ContractorType(null, "OSDp", "Operator Systemu Dystrybucyjnego przyłączony do sieci PSE", true),
            new ContractorType(null, "OSDn", "Operator Systemu Dystrybucyjnego nieprzyłączony do sieci PSE", true),
            new ContractorType(null, "OK", "Odbiorca końcowy", true),
            new ContractorType(null, "Wyt", "Wytwórca", true),
            new ContractorType(null, "Mag", "Magazyn", true)
        ).forEach(repository::save);

        log.info("Seeded 5 initial contractor types");
    }
}
