package ru.romanow.state.machine.models;

public interface StateDescriptor {

    StateMachineType type();

    enum StateMachineType {
        MAIN,
        VAR_MODEL,
        BLACK_MODEL
    }

}
