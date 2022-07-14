package ru.romanow.state.machine.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.statemachine.StateMachine;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;

public interface StateMachineService {
    @NotNull
    StateMachine<States, Events> acquireStateMachine(@NotNull String type, @NotNull String machineId);

    void releaseStateMachine(@NotNull String machineId);
}
