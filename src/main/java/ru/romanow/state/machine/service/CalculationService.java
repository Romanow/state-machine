package ru.romanow.state.machine.service;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.enums.CalculationType;

public interface CalculationService {

    String nextState(@NotNull CalculationType type, @NotNull UUID calculationUid);
}
