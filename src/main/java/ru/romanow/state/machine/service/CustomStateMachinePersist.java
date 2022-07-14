package ru.romanow.state.machine.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;
import ru.romanow.state.machine.domain.CalculationStatus;
import ru.romanow.state.machine.models.CashflowEvents;
import ru.romanow.state.machine.models.CashflowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;

import static java.util.UUID.fromString;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.domain.Pageable.ofSize;

@Component
@RequiredArgsConstructor
public class CustomStateMachinePersist
        extends AbstractPersistingStateMachineInterceptor<CashflowStates, CashflowEvents, String>
        implements StateMachineRuntimePersister<CashflowStates, CashflowEvents, String> {

    private static final Logger logger = getLogger(CustomStateMachinePersist.class);

    private final CalculationRepository calculationRepository;
    private final CalculationStatusRepository calculationStatusRepository;

    @Override
    public void write(StateMachineContext<CashflowStates, CashflowEvents> context, String machineId) {
        logger.info("Write StateMachine '{}' state {}", machineId, context.getState());

        var calculation = calculationRepository.findByUid(fromString(machineId));
        var calculationStatus = new CalculationStatus()
                .setStatus(context.getState())
                .setCalculation(calculation);

        calculationStatusRepository.save(calculationStatus);
    }

    @Override
    public StateMachineContext<CashflowStates, CashflowEvents> read(String machineId) {
        var states = calculationStatusRepository
                .getCalculationLastState(fromString(machineId), ofSize(1));

        if (!states.isEmpty()) {
            var state = states.get(0);
            logger.info("Restore context for StateMachine '{}' with state {}", machineId, state);
            return new DefaultStateMachineContext<>(state, null, null, null, null, machineId);
        }

        logger.info("Previous state not found for StateMachine '{}', create new", machineId);
        return null;
    }

    @Override
    public void postStateChange(State<CashflowStates, CashflowEvents> state, Message<CashflowEvents> message,
                                Transition<CashflowStates, CashflowEvents> transition,
                                StateMachine<CashflowStates, CashflowEvents> stateMachine,
                                StateMachine<CashflowStates, CashflowEvents> rootStateMachine) {
        // Не записываем переход из Start в Init State (CALCULATION_STARTED), т.к. при инициализации
        // StateMachine, которой нет в памяти, вызывается `persist.write(context, machineId)` на
        // init transaction, т.е. в CalculationStatus всегда записывается начальное состояние.
        // Как следствие в CalculationStatus появляется запись `CALCULATION_STARTED`, даже если
        // для этого `calculationUid` уже есть записи. Для решения этой проблемы создано фиктивное
        // состояние `CALCULATION_STARTED`, которое не записывается в БД.
    }

    @Override
    public StateMachineInterceptor<CashflowStates, CashflowEvents> getInterceptor() {
        return this;
    }
}
