package ru.romanow.state.machine;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.state.machine.config.DatabaseTestConfiguration;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Mono.just;
import static ru.romanow.state.machine.domain.enums.CalculationType.VSSDV;
import static ru.romanow.state.machine.utils.CalculationBuilder.buildCalculation;

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration.class)
@Transactional
@AutoConfigureTestEntityManager
class VssdvStateMachineApplicationTest {

    public static final UUID CALCULATION_UID_1 = UUID.fromString("ba012596-4be8-4c96-9721-07b7f9902a6a");
    public static final UUID CALCULATION_UID_2 = UUID.fromString("1066c60a-b6af-4b87-9207-c1109d7dfaa1");

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private CashFlowStateMachineService stateMachineService;

    @BeforeEach
    void init() {
        calculationRepository.saveAll(
                of(buildCalculation(CALCULATION_UID_1, VSSDV), buildCalculation(CALCULATION_UID_2, VSSDV))
        );
    }

    @AfterEach
    void destroy() {
        stateMachineService.destroy();
    }

//    @ParameterizedTest
//    @ValueSource(strings = {
//            "ba012596-4be8-4c96-9721-07b7f9902a6a",
//            "1066c60a-b6af-4b87-9207-c1109d7dfaa1"
//    })
//    void test(String calculationUid) {
//        var stateMachine = stateMachineService.acquireStateMachine(calculationUid);
//
//        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.CALCULATION_STARTED);
//        stateMachine.sendEvent(just(withPayload(CashFlowEvents.DATA_PREPARED_EVENT).build())).subscribe();
//        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.DATA_PREPARED);
//
//        stateMachine = stateMachineService.acquireStateMachine(calculationUid);
//
//        var message = withPayload(CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT).build();
//        var result = stateMachine
//                .sendEvent(just(message)).blockLast();
//
//        assertThat(result).isNotNull();
//        assertThat(result.getResultType()).isEqualTo(ResultType.ACCEPTED);
//        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.DATA_COPIED_TO_STAGED);
//    }

//    @Test
//    void testEventNotAccepted() {
//        var stateMachine = stateMachineService
//                .acquireStateMachine(CALCULATION_UID_1.toString());
//
//        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.CALCULATION_STARTED);
//
//        var message = just(withPayload(CashFlowEvents.ETL_COMPLETED_EVENT).build());
//        var result = stateMachine.sendEvent(message).blockLast();
//
//        assertThat(result).isNotNull();
//        assertThat(result.getResultType()).isEqualTo(ResultType.DENIED);
//        assertThat(stateMachine.getState().getId()).isEqualTo(CashFlowStates.CALCULATION_STARTED);
//    }

}