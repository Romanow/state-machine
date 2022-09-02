package ru.romanow.state.machine;

import java.util.LinkedList;
import java.util.List;
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
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.state.machine.config.DatabaseTestConfiguration;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.vssdv.VssdvStateMachineService;

import static java.util.List.of;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Mono.just;
import static ru.romanow.state.machine.domain.enums.CalculationType.VSSDV;
import static ru.romanow.state.machine.models.vssdv.VssdvEvents.*;
import static ru.romanow.state.machine.models.vssdv.VssdvStates.*;
import static ru.romanow.state.machine.utils.CalculationBuilder.buildCalculation;

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration.class)
@Transactional
@AutoConfigureTestEntityManager
class VssdvStateMachineApplicationTest {

    private static final UUID CALCULATION_UID_1 = fromString("ba012596-4be8-4c96-9721-07b7f9902a6a");
    private static final UUID CALCULATION_UID_2 = fromString("1066c60a-b6af-4b87-9207-c1109d7dfaa1");
    private static final String DELIMITER = ";";

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private VssdvStateMachineService stateMachineService;

    @BeforeEach
    void init() {
        calculationRepository.saveAll(
                of(buildCalculation(CALCULATION_UID_1, VSSDV), buildCalculation(CALCULATION_UID_2, VSSDV)));
    }

    @AfterEach
    void destroy() {
        stateMachineService.destroy();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ba012596-4be8-4c96-9721-07b7f9902a6a",
            "1066c60a-b6af-4b87-9207-c1109d7dfaa1"
    })
    void test(String calculationUid) {
        var stateHistory = new LinkedList<String[]>();
        var stateMachine = stateMachineService.acquireStateMachine(calculationUid);

        assertThat(stateMachine.getState().getIds())
                .containsExactlyInAnyOrder(CALCULATION_STARTED,
                                           VAR_MODEL_CALCULATION_STARTED,
                                           BLACK_MODEL_CALCULATION_STARTED);

        nextState(stateMachine, VAR_MODEL_DATA_PREPARED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_DATA_PREPARED, BLACK_MODEL_CALCULATION_STARTED), stateHistory);

        stateMachine = stateMachineService.acquireStateMachine(calculationUid);

        nextState(stateMachine, BLACK_MODEL_DATA_PREPARED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_DATA_PREPARED, BLACK_MODEL_DATA_PREPARED), stateHistory);

        nextState(stateMachine, VAR_MODEL_DATA_COPIED_TO_STAGED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_DATA_COPIED_TO_STAGED, BLACK_MODEL_DATA_PREPARED), stateHistory);

        nextState(stateMachine, BLACK_MODEL_DATA_COPIED_TO_STAGED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_DATA_COPIED_TO_STAGED, BLACK_MODEL_DATA_COPIED_TO_STAGED),
                  stateHistory);


        nextState(stateMachine, VAR_MODEL_ETL_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_START, BLACK_MODEL_DATA_COPIED_TO_STAGED), stateHistory);

        nextState(stateMachine, VAR_MODEL_ETL_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_SENT_TO_DRP, BLACK_MODEL_DATA_COPIED_TO_STAGED), stateHistory);

        nextState(stateMachine, VAR_MODEL_ETL_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_ACCEPTED, BLACK_MODEL_DATA_COPIED_TO_STAGED), stateHistory);

        nextState(stateMachine, VAR_MODEL_ETL_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_COMPLETED, BLACK_MODEL_DATA_COPIED_TO_STAGED), stateHistory);


        nextState(stateMachine, BLACK_MODEL_ETL_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_COMPLETED, BLACK_MODEL_ETL_START), stateHistory);

        nextState(stateMachine, BLACK_MODEL_ETL_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_COMPLETED, BLACK_MODEL_ETL_SENT_TO_DRP), stateHistory);

        nextState(stateMachine, BLACK_MODEL_ETL_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_COMPLETED, BLACK_MODEL_ETL_ACCEPTED), stateHistory);

        nextState(stateMachine, BLACK_MODEL_ETL_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_ETL_COMPLETED, BLACK_MODEL_ETL_COMPLETED), stateHistory);


        nextState(stateMachine, VAR_MODEL_CALCULATION_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_START, BLACK_MODEL_ETL_COMPLETED), stateHistory);

        nextState(stateMachine, VAR_MODEL_CALCULATION_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_SENT_TO_DRP, BLACK_MODEL_ETL_COMPLETED), stateHistory);

        nextState(stateMachine, VAR_MODEL_CALCULATION_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_ACCEPTED, BLACK_MODEL_ETL_COMPLETED), stateHistory);

        nextState(stateMachine, VAR_MODEL_CALCULATION_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_COMPLETED, BLACK_MODEL_ETL_COMPLETED), stateHistory);


        nextState(stateMachine, BLACK_MODEL_CALCULATION_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_COMPLETED, BLACK_MODEL_CALCULATION_START),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_CALCULATION_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_COMPLETED, BLACK_MODEL_CALCULATION_SENT_TO_DRP),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_CALCULATION_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_COMPLETED, BLACK_MODEL_CALCULATION_ACCEPTED),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_CALCULATION_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_COMPLETED, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);


        nextState(stateMachine, VAR_MODEL_REVERSED_ETL_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_REVERSED_ETL_START, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);

        nextState(stateMachine, VAR_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_REVERSED_ETL_SENT_TO_DRP, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);

        nextState(stateMachine, VAR_MODEL_REVERSED_ETL_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_REVERSED_ETL_ACCEPTED, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);

        nextState(stateMachine, VAR_MODEL_REVERSED_ETL_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_REVERSED_ETL_COMPLETED, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);

        nextState(stateMachine, VAR_MODEL_DATA_COPIED_FROM_STAGED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_DATA_COPIED_FROM_STAGED, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);

        nextState(stateMachine, VAR_MODEL_CALCULATION_FINISHED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_CALCULATION_COMPLETED),
                  stateHistory);


        nextState(stateMachine, BLACK_MODEL_REVERSED_ETL_START_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_REVERSED_ETL_START),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_REVERSED_ETL_ACCEPTED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_REVERSED_ETL_ACCEPTED),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_REVERSED_ETL_COMPLETED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_REVERSED_ETL_COMPLETED),
                  stateHistory);

        nextState(stateMachine, BLACK_MODEL_DATA_COPIED_FROM_STAGED_EVENT,
                  of(CALCULATION_STARTED, VAR_MODEL_CALCULATION_FINISHED, BLACK_MODEL_DATA_COPIED_FROM_STAGED),
                  stateHistory);

        stateHistory.addFirst(new String[]{
                CALCULATION_STARTED.name(),
                VAR_MODEL_CALCULATION_FINISHED.name(),
                BLACK_MODEL_CALCULATION_FINISHED.name()
        });
        nextState(stateMachine, BLACK_MODEL_CALCULATION_FINISHED_EVENT, of(VSSDV_CALCULATION_STARTED), stateHistory);

        nextState(stateMachine, VSSDV_DATA_PREPARED_EVENT, of(VSSDV_DATA_PREPARED), stateHistory);
        nextState(stateMachine, VSSDV_DATA_COPIED_TO_STAGED_EVENT, of(VSSDV_DATA_COPIED_TO_STAGED), stateHistory);

        nextState(stateMachine, VSSDV_ETL_START_EVENT, of(VSSDV_ETL_START), stateHistory);
        nextState(stateMachine, VSSDV_ETL_SENT_TO_DRP_EVENT, of(VSSDV_ETL_SENT_TO_DRP), stateHistory);
        nextState(stateMachine, VSSDV_ETL_ACCEPTED_EVENT, of(VSSDV_ETL_ACCEPTED), stateHistory);
        nextState(stateMachine, VSSDV_ETL_COMPLETED_EVENT, of(VSSDV_ETL_COMPLETED), stateHistory);

        nextState(stateMachine, VSSDV_CALCULATION_START_EVENT, of(VSSDV_CALCULATION_START), stateHistory);
        nextState(stateMachine, VSSDV_CALCULATION_SENT_TO_DRP_EVENT, of(VSSDV_CALCULATION_SENT_TO_DRP), stateHistory);
        nextState(stateMachine, VSSDV_CALCULATION_ACCEPTED_EVENT, of(VSSDV_CALCULATION_ACCEPTED), stateHistory);
        nextState(stateMachine, VSSDV_CALCULATION_COMPLETED_EVENT, of(VSSDV_CALCULATION_COMPLETED), stateHistory);

        nextState(stateMachine, VSSDV_REVERSED_ETL_START_EVENT, of(VSSDV_REVERSED_ETL_START), stateHistory);
        nextState(stateMachine, VSSDV_REVERSED_ETL_SENT_TO_DRP_EVENT, of(VSSDV_REVERSED_ETL_SENT_TO_DRP), stateHistory);
        nextState(stateMachine, VSSDV_REVERSED_ETL_ACCEPTED_EVENT, of(VSSDV_REVERSED_ETL_ACCEPTED), stateHistory);
        nextState(stateMachine, VSSDV_REVERSED_ETL_COMPLETED_EVENT, of(VSSDV_REVERSED_ETL_COMPLETED), stateHistory);

        nextState(stateMachine, VSSDV_DATA_COPIED_FROM_STAGED_EVENT, of(VSSDV_DATA_COPIED_FROM_STAGED), stateHistory);
        nextState(stateMachine, VSSDV_CALCULATION_FINISHED_EVENT, of(VSSDV_CALCULATION_FINISHED), stateHistory);
    }

    @Test
    void testEventNotAccepted() {
        var stateMachine = stateMachineService.acquireStateMachine(CALCULATION_UID_1.toString());

        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder(CALCULATION_STARTED,
                                                                               VAR_MODEL_CALCULATION_STARTED,
                                                                               BLACK_MODEL_CALCULATION_STARTED);

        var message = just(withPayload(VssdvEvents.VAR_MODEL_ETL_COMPLETED_EVENT).build());
        var result = stateMachine.sendEvent(message).blockLast();

        assertThat(result).isNotNull();
        assertThat(result.getResultType()).isEqualTo(ResultType.DENIED);
        assertThat(stateMachine.getState().getId()).isEqualTo(VssdvStates.CALCULATION_STARTED);
    }

    private void nextState(
            @NotNull StateMachine<VssdvStates, VssdvEvents> stateMachine,
            @NotNull VssdvEvents event,
            @NotNull List<VssdvStates> expectedStates,
            @NotNull LinkedList<String[]> stateHistory
    ) {
        final var message = withPayload(event).build();
        final var result = stateMachine.sendEvent(just(message)).blockLast();

        assertThat(result).isNotNull();
        assertThat(result.getResultType()).isEqualTo(ResultType.ACCEPTED);
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder(
                expectedStates.toArray(VssdvStates[]::new));

        stateHistory.addFirst(expectedStates.stream().map(Enum::name).toArray(String[]::new));
        final var states = calculationStatusRepository
                .getCalculationLastState(fromString(stateMachine.getId()), unpaged());

        assertThat(states).hasSize(stateHistory.size());
        for (int i = 0; i < states.size(); i++) {
            var state = states.get(i);
            assertThat(state.split(DELIMITER)).containsExactlyInAnyOrder(stateHistory.get(i));
        }
    }

}