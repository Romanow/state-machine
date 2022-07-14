package ru.romanow.state.machine.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.statemachine.StateMachine;
import ru.romanow.state.machine.models.CashflowEvents;
import ru.romanow.state.machine.models.CashflowStates;

public interface StateMachineService {
    @NotNull
    StateMachine<CashflowStates, CashflowEvents> acquireStateMachine(@NotNull String type, @NotNull String machineId);

    void releaseStateMachine(@NotNull String machineId);
}
