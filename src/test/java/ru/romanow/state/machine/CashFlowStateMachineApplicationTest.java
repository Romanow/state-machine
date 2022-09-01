package ru.romanow.state.machine;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.romanow.state.machine.config.DatabaseTestConfiguration;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;

import static java.util.List.of;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.messaging.support.MessageBuilder.createMessage;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Mono.just;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.CALCULATION_ACCEPTED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.CALCULATION_COMPLETED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.CALCULATION_FINISHED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.CALCULATION_SENT_TO_DRP_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.CALCULATION_START_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.DATA_COPIED_FROM_STAGED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.DATA_PREPARED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.ETL_ACCEPTED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.ETL_COMPLETED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.ETL_SENT_TO_DRP_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.ETL_START_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.REVERSED_ETL_ACCEPTED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.REVERSED_ETL_COMPLETED_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowEvents.REVERSED_ETL_START_EVENT;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.CALCULATION_ACCEPTED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.CALCULATION_COMPLETED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.CALCULATION_FINISHED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.CALCULATION_SENT_TO_DRP;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.CALCULATION_START;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.DATA_COPIED_FROM_STAGED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.DATA_COPIED_TO_STAGED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.DATA_PREPARED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.ETL_ACCEPTED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.ETL_COMPLETED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.ETL_SENT_TO_DRP;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.ETL_START;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.REVERSED_ETL_ACCEPTED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.REVERSED_ETL_COMPLETED;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.REVERSED_ETL_SENT_TO_DRP;
import static ru.romanow.state.machine.models.cashflow.CashFlowStates.REVERSED_ETL_START;
import static ru.romanow.state.machine.utils.CalculationBuilder.buildCalculation;

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration.class)
@Transactional
@AutoConfigureTestEntityManager
class CashFlowStateMachineApplicationTest {

    public static final UUID CALCULATION_UID_1 = UUID.fromString("639cb402-3ae4-4ff4-ab1a-d70eaa661334");
    public static final UUID CALCULATION_UID_2 = UUID.fromString("07dabafa-529d-4da4-bab5-a6359313c064");

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private CashFlowStateMachineService stateMachineService;

    @BeforeEach
    void init() {
        calculationRepository.saveAll(
                of(buildCalculation(CALCULATION_UID_1, CASH_FLOW), buildCalculation(CALCULATION_UID_2, CASH_FLOW))
        );
    }

    @AfterEach
    void destroy() {
        stateMachineService.destroy();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "639cb402-3ae4-4ff4-ab1a-d70eaa661334",
            "07dabafa-529d-4da4-bab5-a6359313c064"
    })
    void test(String calculationUid) {
        var stateHistory = new LinkedList<String>();
        var stateMachine = stateMachineService.acquireStateMachine(calculationUid);

        nextState(stateMachine, DATA_PREPARED_EVENT, DATA_PREPARED, stateHistory);

        stateMachine = stateMachineService.acquireStateMachine(calculationUid);

        nextState(stateMachine, DATA_COPIED_TO_STAGED_EVENT, DATA_COPIED_TO_STAGED, stateHistory);

        nextState(stateMachine, ETL_START_EVENT, ETL_START, stateHistory);
        nextState(stateMachine, ETL_SENT_TO_DRP_EVENT, ETL_SENT_TO_DRP, stateHistory);
        nextState(stateMachine, ETL_ACCEPTED_EVENT, ETL_ACCEPTED, stateHistory);
        nextState(stateMachine, ETL_COMPLETED_EVENT, ETL_COMPLETED, stateHistory);

        nextState(stateMachine, CALCULATION_START_EVENT, CALCULATION_START, stateHistory);
        nextState(stateMachine, CALCULATION_SENT_TO_DRP_EVENT, CALCULATION_SENT_TO_DRP, stateHistory);
        nextState(stateMachine, CALCULATION_ACCEPTED_EVENT, CALCULATION_ACCEPTED, stateHistory);
        nextState(stateMachine, CALCULATION_COMPLETED_EVENT, CALCULATION_COMPLETED, stateHistory);

        nextState(stateMachine, REVERSED_ETL_START_EVENT, REVERSED_ETL_START, stateHistory);
        nextState(stateMachine, REVERSED_ETL_SENT_TO_DRP_EVENT, REVERSED_ETL_SENT_TO_DRP, stateHistory);
        nextState(stateMachine, REVERSED_ETL_ACCEPTED_EVENT, REVERSED_ETL_ACCEPTED, stateHistory);
        nextState(stateMachine, REVERSED_ETL_COMPLETED_EVENT, REVERSED_ETL_COMPLETED, stateHistory);

        nextState(stateMachine, DATA_COPIED_FROM_STAGED_EVENT, DATA_COPIED_FROM_STAGED, stateHistory);
        nextState(stateMachine, CALCULATION_FINISHED_EVENT, CALCULATION_FINISHED, stateHistory);
    }

    @Test
    void testEventNotAccepted() {
        var stateMachine = stateMachineService
                .acquireStateMachine(CALCULATION_UID_1.toString());

        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.CALCULATION_STARTED);

        var message = just(withPayload(CashFlowEvents.ETL_COMPLETED_EVENT).build());
        var result = stateMachine.sendEvent(message).blockLast();

        assertThat(result).isNotNull();
        assertThat(result.getResultType()).isEqualTo(ResultType.DENIED);
        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.CALCULATION_STARTED);

        var states = calculationStatusRepository
                .getCalculationLastState(CALCULATION_UID_1, unpaged());
        assertThat(states).isEmpty();
    }

    private void nextState(@NotNull StateMachine<CashFlowStates, CashFlowEvents> stateMachine,
                           @NotNull CashFlowEvents event,
                           @NotNull CashFlowStates expectedState,
                           @NotNull LinkedList<String> stateHistory) {
        final var message = createMessage(event, new MessageHeaders(Map.of()));
        final var result = stateMachine.sendEvent(Mono.just(message)).blockLast();

        assertThat(result).isNotNull();
        assertThat(result.getResultType()).isEqualTo(ResultType.ACCEPTED);
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder(expectedState);

        stateHistory.addFirst(expectedState.name());
        final var states = calculationStatusRepository
                .getCalculationLastState(fromString(stateMachine.getId()), unpaged());

        assertThat(states).hasSize(stateHistory.size());
        assertThat(states).containsExactly(stateHistory.toArray(String[]::new));
    }

}