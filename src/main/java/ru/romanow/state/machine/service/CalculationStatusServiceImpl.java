package ru.romanow.state.machine.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.state.machine.domain.CalculationStatus;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;

import static org.springframework.data.domain.Pageable.ofSize;

@Service
@RequiredArgsConstructor
public class CalculationStatusServiceImpl
        implements CalculationStatusService {
    private final CalculationService calculationService;
    private final CalculationStatusRepository calculationStatusRepository;

    @Override
    @Transactional
    public void create(@NotNull UUID calculationUid, @NotNull String status) {
        var calculation = calculationService.findByUid(calculationUid);
        var calculationStatus = new CalculationStatus()
                .setStatus(status)
                .setCalculation(calculation);

        calculationStatusRepository.save(calculationStatus);
    }

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getCalculationLastState(@NotNull UUID calculationUid) {
        return calculationStatusRepository
                .getCalculationLastState(calculationUid, ofSize(1))
                .stream()
                .findFirst();
    }
}
