package ru.romanow.state.machine.domain.enums;

import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.CalculationTypes;

public enum CalculationType {
    CASH_FLOW(CalculationTypes.CASHFLOW);

    private final String value;

    CalculationType(@NotNull String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
