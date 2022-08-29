package ru.romanow.state.machine.service;

import java.util.ArrayList;
import java.util.List;
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
import ru.romanow.state.machine.domain.CalculationStatus;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;

import static java.lang.String.join;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.domain.Pageable.ofSize;
import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
public abstract class BaseCustomStateMachinePersist<States extends Enum<States>, Events extends Enum<Events>>
        extends AbstractPersistingStateMachineInterceptor<States, Events, String>
        implements StateMachineRuntimePersister<States, Events, String> {

    private static final Logger logger = getLogger(BaseCustomStateMachinePersist.class);
    private static final String DELIMITER = ";";

    private final CalculationRepository calculationRepository;
    private final CalculationStatusRepository calculationStatusRepository;

    @Override
    public void write(StateMachineContext<States, Events> context, String machineId) {
        logger.info("Write StateMachine '{}' state {}", machineId, context.getState());

        var calculation = calculationRepository.findByUid(fromString(machineId));
        var calculationStatus = new CalculationStatus()
                .setStatus(buildStatus(context))
                .setCalculation(calculation);

        calculationStatusRepository.save(calculationStatus);
    }

    private String buildStatus(StateMachineContext<States, Events> context) {
        var statuses = new ArrayList<String>();
        statuses.add(context.getState().name());
        if (!isEmpty(context.getChilds())) {
            context.getChilds().forEach(c -> statuses.add(c.getState().name()));
        }
        return join(DELIMITER, statuses);
    }

    @Override
    public StateMachineContext<States, Events> read(String machineId) {
        final List<String> list = calculationStatusRepository
                .getCalculationLastState(fromString(machineId), ofSize(1));

        if (!list.isEmpty()) {
            var state = list.get(0);
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
    public void preStateChange(State<States, Events> state, Message<Events> message,
                               Transition<States, Events> transition, StateMachine<States, Events> stateMachine,
                               StateMachine<States, Events> rootStateMachine) {
    }

    @Override
    public void postStateChange(State<States, Events> state, Message<Events> message,
                                Transition<States, Events> transition,
                                StateMachine<States, Events> stateMachine,
                                StateMachine<States, Events> rootStateMachine) {
        if (state != null && transition != null && transition.getKind() != TransitionKind.INITIAL) {
            write(buildStateMachineContext(stateMachine, rootStateMachine, state, message), rootStateMachine.getId());
        }
    }

    @Override
    public StateMachineInterceptor<States, Events> getInterceptor() {
        return this;
    }

    protected abstract States restoreState(@NotNull String state);

    @Override
    protected StateMachineContext<States, Events> buildStateMachineContext(StateMachine<States, Events> stateMachine,
                                                                           StateMachine<States, Events> rootStateMachine,
                                                                           State<States, Events> state,
                                                                           Message<Events> message) {
        final var states = List.copyOf(rootStateMachine.getState().getIds());
        final List<StateMachineContext<States, Events>> childrenStates = range(1, states.size())
                .mapToObj(i -> new DefaultStateMachineContext<>(states.get(i), message.getPayload(), null, null))
                .collect(toList());

        return new DefaultStateMachineContext<>(childrenStates, rootStateMachine.getState().getId(),
                                                message.getPayload(),
                                                message.getHeaders(),
                                                stateMachine.getExtendedState());
    }
}
