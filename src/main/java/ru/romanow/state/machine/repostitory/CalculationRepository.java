package ru.romanow.state.machine.repostitory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.romanow.state.machine.domain.Calculation;

public interface CalculationRepository
        extends JpaRepository<Calculation, Long> {
    Calculation findByUid(UUID uid);
}
