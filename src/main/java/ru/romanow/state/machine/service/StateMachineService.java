package ru.romanow.state.machine.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.statemachine.StateMachine;

public interface StateMachineService<States, Events> {

    @NotNull
    StateMachine<States, Events> acquireStateMachine(@NotNull String machineId);

    void releaseStateMachine(@NotNull String machineId);
}
