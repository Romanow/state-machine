package ru.romanow.state.machine.service;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface CalculationService {

    String nextState(@NotNull UUID calculationUid);

}
