package ru.romanow.state.machine.service.vssdv;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;
import ru.romanow.state.machine.service.CalculationStatusService;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Service
public class VssdvCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<VssdvStates, VssdvEvents> {

    @Autowired
    public VssdvCustomStateMachinePersist(CalculationStatusService calculationStatusService) {
        super(calculationStatusService);
    }

    @Override
    protected StateMachineContext<VssdvStates, VssdvEvents> buildStateMachineContext(
            StateMachine<VssdvStates, VssdvEvents> stateMachine,
            StateMachine<VssdvStates, VssdvEvents> rootStateMachine,
            State<VssdvStates, VssdvEvents> state,
            Message<VssdvEvents> message
    ) {
        // Т.к. для параллельных состояний Spring State Machine описывается кортежем состояний, т.е. при сохранении
        // нужно получить их все. `state.getIds()` содержит только одно состояние (измененное), а глобальное состояние
        // можно получить из `stateMachine.getState().getIds()`, но в методе preStateChange (из которого вызывается write)
        // передается еще не измененная State Machine. Поэтому мы ищем событие перехода, берем его source и ищем это
        // состояние в текущем состоянии State Machine (чтобы понять какую ветку состояний менять), если нашли,
        // то меняем на `state.getId()`.
        var event = !isNull(message) ? message.getPayload() : null;
        var headers = !isNull(message) ? message.getHeaders() : null;
        final var transitionSource = stateMachine
                .getTransitions()
                .stream()
                .filter(tr -> !isNull(tr.getTrigger()) && !isNull(event) && tr.getTrigger().getEvent() == event)
                .findFirst()
                .map(tr -> tr.getSource().getId())
                .orElse(null);

        final var states = List.copyOf(rootStateMachine.getState().getIds());
        final List<StateMachineContext<VssdvStates, VssdvEvents>> childrenStates = range(1, states.size())
                .mapToObj(i -> {
                    final var resultState = getActualState(transitionSource, state.getId(), states.get(i));
                    return new DefaultStateMachineContext<>(resultState, event, null, null);
                })
                .collect(toList());

        final var mainState = getActualState(transitionSource, state.getId(), stateMachine.getState().getId());
        return new DefaultStateMachineContext<>(childrenStates, mainState, event, headers,
                                                stateMachine.getExtendedState());
    }

    @NotNull
    private VssdvStates getActualState(
            @Nullable VssdvStates transitionSource,
            @NotNull VssdvStates changedState,
            @NotNull VssdvStates originalState
    ) {
        return !isNull(transitionSource) && originalState.equals(transitionSource) ? changedState : originalState;
    }

    @Override
    protected VssdvStates restoreState(@NotNull String state) {
        return VssdvStates.valueOf(state);
    }
}
