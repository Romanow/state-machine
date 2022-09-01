package ru.romanow.state.machine.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface CalculationStatusService {
    void create(@NotNull UUID calculationUid, @NotNull String status);

    @NotNull
    Optional<String> getCalculationLastState(@NotNull UUID calculationUid);
}
