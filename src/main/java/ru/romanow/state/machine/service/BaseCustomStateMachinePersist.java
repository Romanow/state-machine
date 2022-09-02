package ru.romanow.state.machine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import ru.romanow.state.machine.models.StateDescriptor;
import ru.romanow.state.machine.models.StateDescriptor.StateMachineType;

import static java.lang.String.join;
import static java.util.List.of;
import static java.util.Objects.isNull;
import static java.util.UUID.fromString;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.MAIN;

@RequiredArgsConstructor
public abstract class BaseCustomStateMachinePersist<States extends Enum<States> & StateDescriptor, Events extends Enum<Events>>
        extends AbstractPersistingStateMachineInterceptor<States, Events, String>
        implements StateMachineRuntimePersister<States, Events, String> {

    private static final Logger logger = getLogger(BaseCustomStateMachinePersist.class);
    private static final String DELIMITER = ";";

    private final CalculationStatusService calculationStatusService;

    @Override
    public void write(StateMachineContext<States, Events> context, String machineId) {
        logger.info("Write StateMachine '{}' state {}", machineId, context.getState());
        calculationStatusService.create(fromString(machineId), buildStatus(context));
    }

    @Override
    public StateMachineContext<States, Events> read(String machineId) {
        final Optional<String> result = calculationStatusService.getCalculationLastState(fromString(machineId));

        if (result.isPresent()) {
            var state = result.get();
            logger.info("Restore context for StateMachine '{}' with state {}", machineId, state);

            var states = state.split(DELIMITER);
            var mainState = restoreState(states[0]);
            final List<StateMachineContext<States, Events>> childrenStates =
                    range(1, states.length)
                            .mapToObj(i -> new DefaultStateMachineContext<States, Events>
                                    (restoreState(states[i]), null, null, null))
                            .collect(toList());

            return new DefaultStateMachineContext<>(childrenStates, mainState, null, null, null, null, machineId);
        }

        logger.info("Previous state not found for StateMachine '{}', create new", machineId);
        return null;
    }

    @Override
    public void preStateChange(
            State<States, Events> state,
            Message<Events> message,
            Transition<States, Events> transition,
            StateMachine<States, Events> stateMachine,
            StateMachine<States, Events> rootStateMachine
    ) {
        // Т.к. для параллельных состояний Spring State Machine описывается кортежем состояний,
        // то при сохранении нужно получить их все. `state.getId()` содержит только одно состояние (измененное),
        // а глобальное состояние можно получить из `rootStateMachine.getState().getIds()`, но в методе
        // preStateChange (из которого вызывается write) передается еще не измененная State Machine.
    }

    @Override
    public void postStateChange(
            State<States, Events> state, Message<Events> message,
            Transition<States, Events> transition,
            StateMachine<States, Events> stateMachine,
            StateMachine<States, Events> rootStateMachine
    ) {
        if (state != null && transition != null && transition.getKind() != TransitionKind.INITIAL) {
            write(buildStateMachineContext(stateMachine, rootStateMachine, state, message), rootStateMachine.getId());
        }
    }

    @Override
    public StateMachineInterceptor<States, Events> getInterceptor() {
        return this;
    }

    @Override
    protected StateMachineContext<States, Events> buildStateMachineContext(
            StateMachine<States, Events> stateMachine,
            StateMachine<States, Events> rootStateMachine,
            State<States, Events> state,
            Message<Events> message
    ) {
        final var payload = !isNull(message) ? message.getPayload() : null;
        final var headers = !isNull(message) ? message.getHeaders() : null;

        // Если меняется главное состояние State Machine, то записываем только его,
        // если меняются вложенные, то сохраняем все состояния
        if (state.getId().type() != MAIN) {
            final var states = rootStateMachine
                    .getState()
                    .getIds()
                    .stream()
                    .collect(toMap(StateDescriptor::type, identity()));

            states.put(state.getId().type(), state.getId());

            final List<StateMachineContext<States, Events>> childrenStates = secondaryMachineTypes()
                    .stream()
                    .filter(states::containsKey)
                    .map(type -> new DefaultStateMachineContext<>(states.get(type), payload, null, null))
                    .collect(toList());

            return new DefaultStateMachineContext<>(childrenStates,
                                                    states.get(MAIN),
                                                    payload,
                                                    headers,
                                                    stateMachine.getExtendedState());
        }

        return new DefaultStateMachineContext<>(state.getId(), payload, headers,
                                                stateMachine.getExtendedState());


    }

    protected abstract States restoreState(@NotNull String state);

    protected List<StateMachineType> secondaryMachineTypes() {
        return of();
    }

    @NotNull
    private String buildStatus(@NotNull StateMachineContext<States, Events> context) {
        var statuses = new ArrayList<String>();
        statuses.add(context.getState().name());
        if (!isEmpty(context.getChilds())) {
            context.getChilds().forEach(c -> statuses.add(c.getState().name()));
        }
        return join(DELIMITER, statuses);
    }

}
