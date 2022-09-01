package ru.romanow.state.machine.service;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.Calculation;

public interface CalculationService {
    @NotNull
    Calculation findByUid(@NotNull UUID calculationUid);
}
