package ru.romanow.state.machine.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.romanow.state.machine.models.CashflowEvents;
import ru.romanow.state.machine.models.CashflowStates;

import static java.lang.Integer.toHexString;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl
        implements CalculationService {

    private static final Logger logger = getLogger(CalculationServiceImpl.class);

    private static final Map<CashflowStates, CashflowEvents> NEXT_STATE_EVENT = new HashMap<>() {
        {
            put(CashflowStates.CALCULATION_STARTED, CashflowEvents.DATA_PREPARED_EVENT);
            put(CashflowStates.DATA_PREPARED, CashflowEvents.DATA_COPIED_TO_STAGED_EVENT);
            put(CashflowStates.DATA_COPIED_TO_STAGED, CashflowEvents.ETL_START_EVENT);

            put(CashflowStates.ETL_START, CashflowEvents.ETL_SENT_TO_DRP_EVENT);
            put(CashflowStates.ETL_SEND_TO_DRP, CashflowEvents.ETL_ACCEPTED_EVENT);
            put(CashflowStates.ETL_ACCEPTED, CashflowEvents.ETL_COMPLETED_EVENT);
            put(CashflowStates.ETL_COMPLETED, CashflowEvents.CALCULATION_START_EVENT);

            put(CashflowStates.CALCULATION_START, CashflowEvents.CALCULATION_SENT_TO_DRP_EVENT);
            put(CashflowStates.CALCULATION_SENT_TO_DRP, CashflowEvents.CALCULATION_ACCEPTED_EVENT);
            put(CashflowStates.CALCULATION_ACCEPTED, CashflowEvents.CALCULATION_COMPLETED_EVENT);
            put(CashflowStates.CALCULATION_COMPLETED, CashflowEvents.REVERSED_ETL_START_EVENT);

            put(CashflowStates.REVERSED_ETL_START, CashflowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT);
            put(CashflowStates.REVERSED_ETL_SENT_TO_DRP, CashflowEvents.REVERSED_ETL_ACCEPTED_EVENT);
            put(CashflowStates.REVERSED_ETL_ACCEPTED, CashflowEvents.REVERSED_COMPLETED_EVENT);
            put(CashflowStates.REVERSED_COMPLETED, CashflowEvents.DATA_COPIED_FROM_STAGED_EVENT);

            put(CashflowStates.DATA_COPIED_FROM_STAGED, CashflowEvents.CALCULATION_FINISHED_EVENT);
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
