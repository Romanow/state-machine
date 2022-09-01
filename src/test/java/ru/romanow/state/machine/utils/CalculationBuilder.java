package ru.romanow.state.machine.utils;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;

import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

public final class CalculationBuilder {

    @NotNull
    public static Calculation buildCalculation(@NotNull UUID uid, @NotNull CalculationType type) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(type)
                .setDescription(randomAlphabetic(20));
    }
}
