package ru.romanow.state.machine.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;

import static java.lang.Integer.toHexString;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl
        implements CalculationService {

    private static final Logger logger = getLogger(CalculationServiceImpl.class);

    private static final Map<States, Events> NEXT_STATE_EVENT = new HashMap<>() {
        {
            put(States.CALCULATION_STARTED, Events.DATA_PREPARED_EVENT);
            put(States.DATA_PREPARED, Events.ETL_START_EVENT);

            put(States.ETL_START, Events.ETL_SENT_TO_DRP_EVENT);
            put(States.ETL_SEND_TO_DRP, Events.ETL_ACCEPTED_EVENT);
            put(States.ETL_ACCEPTED, Events.ETL_COMPLETED_EVENT);
            put(States.ETL_COMPLETED, Events.CALCULATION_START_EVENT);

            put(States.CALCULATION_START, Events.CALCULATION_SENT_TO_DRP_EVENT);
            put(States.CALCULATION_SENT_TO_DRP, Events.CALCULATION_ACCEPTED_EVENT);
            put(States.CALCULATION_ACCEPTED, Events.CALCULATION_COMPLETED_EVENT);
            put(States.CALCULATION_COMPLETED, Events.REVERSED_ETL_START_EVENT);

            put(States.REVERSED_ETL_START, Events.REVERSED_ETL_SENT_TO_DRP_EVENT);
            put(States.REVERSED_ETL_SENT_TO_DRP, Events.REVERSED_ETL_ACCEPTED_EVENT);
            put(States.REVERSED_ETL_ACCEPTED, Events.REVERSED_COMPLETED_EVENT);
            put(States.REVERSED_COMPLETED, Events.CALCULATION_FINISHED_EVENT);
        }
    };

    private final StateMachineService stateMachineService;

    @Override
    public String nextState(@NotNull UUID calculationUid) {
        final var stateMachine = stateMachineService
                .acquireStateMachine(CASH_FLOW.value(), calculationUid.toString());

        final var state = stateMachine.getState().getId();
        logger.info("Current SM '{}' for UID '{}' with state '{}'",
                    toHexString(stateMachine.hashCode()), stateMachine.getUuid(), state);

        final var event = NEXT_STATE_EVENT.get(state);
        final var message = withPayload(event).build();
        stateMachine.sendEvent(Mono.just(message)).subscribe();

        return stateMachine.getState().getId().name();
    }
}
