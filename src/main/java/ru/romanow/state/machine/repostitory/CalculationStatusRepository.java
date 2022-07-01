package ru.romanow.state.machine.repostitory;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.romanow.state.machine.domain.CalculationStatus;

public interface CalculationStatusRepository
        extends JpaRepository<CalculationStatus, Long> {

}
