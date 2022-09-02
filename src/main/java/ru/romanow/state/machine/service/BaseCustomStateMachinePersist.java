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

import static java.lang.String.join;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
public abstract class BaseCustomStateMachinePersist<States extends Enum<States>, Events extends Enum<Events>>
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
    public void postStateChange(
            State<States, Events> state, Message<Events> message,
            Transition<States, Events> transition,
            StateMachine<States, Events> stateMachine,
            StateMachine<States, Events> rootStateMachine
    ) {
        // Не записываем переход из Start в Init State (CALCULATION_STARTED), т.к. при инициализации
        // StateMachine, которой нет в памяти, вызывается `persist.write(context, machineId)` на
        // init transaction, т.е. в CalculationStatus всегда записывается начальное состояние.
        // Как следствие в CalculationStatus появляется запись `CALCULATION_STARTED`, даже если
        // для этого `calculationUid` уже есть записи. Для решения этой проблемы создано фиктивное
        // состояние `CALCULATION_STARTED`, которое не записывается в БД.
    }

    @Override
    public StateMachineInterceptor<States, Events> getInterceptor() {
        return this;
    }

    protected abstract States restoreState(@NotNull String state);

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
