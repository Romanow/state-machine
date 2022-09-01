package ru.romanow.state.machine.service;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.enums.CalculationType;

public interface TestTransitionService {

    List<String> nextState(@NotNull CalculationType type, @NotNull UUID calculationUid);
}
