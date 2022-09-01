package ru.romanow.state.machine.service;

import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.repostitory.CalculationRepository;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl
        implements CalculationService {
    private final CalculationRepository calculationRepository;

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public Calculation findByUid(@NotNull UUID calculationUid) {
        return calculationRepository
                .findByUid(calculationUid)
                .orElseThrow(() -> new EntityNotFoundException("Calculation not found by '" + calculationUid + "'"));
    }
}
