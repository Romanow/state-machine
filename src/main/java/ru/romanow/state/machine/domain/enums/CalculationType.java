package ru.romanow.state.machine.domain.enums;

import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.domain.CalculationTypes;

import static java.util.Arrays.stream;

public enum CalculationType {
    CASH_FLOW(CalculationTypes.CASHFLOW),
    VSSDV(CalculationTypes.VSSDV);

    private final String value;

    CalculationType(@NotNull String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CalculationType find(@NotNull String type) {
        return stream(values())
                .filter(t -> t.value().equalsIgnoreCase(type))
                .findFirst()
                .get();
    }
}
