package ru.romanow.state.machine.config;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Component;
import ru.romanow.state.machine.domain.CalculationStatus;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@RequiredArgsConstructor
public class CustomStateMachinePersist
        extends AbstractPersistingStateMachineInterceptor<States, Events, String>
        implements StateMachineRuntimePersister<States, Events, String> {

    private static final Logger logger = getLogger(CustomStateMachinePersist.class);

    private final CalculationRepository calculationRepository;
    private final CalculationStatusRepository calculationStatusRepository;

    @Override
    public void write(StateMachineContext<States, Events> context, String contextObj) {
        logger.info("Write context {}", context);

        var calculation = calculationRepository.findByUid(UUID.fromString(contextObj));
        var calculationStatus = new CalculationStatus()
                .setStatus(context.getState())
                .setCalculation(calculation);

        calculationStatusRepository.save(calculationStatus);
    }

    @Override
    public StateMachineContext<States, Events> read(String contextObj) {
        logger.info("Restore context {}", contextObj);

        var states = calculationStatusRepository
                .getCalculationLastState(UUID.fromString(contextObj), Pageable.ofSize(1));
        if (!states.isEmpty()) {
            return new DefaultStateMachineContext<>(states.get(0), null, null, null);
        }
        return null;
    }

    @Override
    public StateMachineInterceptor<States, Events> getInterceptor() {
        return this;
    }
}
