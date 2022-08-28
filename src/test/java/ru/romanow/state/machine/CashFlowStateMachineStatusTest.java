package ru.romanow.state.machine;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.StateMachineStatusTest.TestConfiguration;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfiguration.class)
public class CashFlowStateMachineStatusTest
        extends StateMachineStatusTest {

    @Autowired
    private CashFlowCalculationStatusRepository cashFlowCalculationStatusRepository;

    @Autowired
    private CashFlowStateMachineService cashFlowStateMachineService;

    @Test
    void testSuccess()
            throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId, CASH_FLOW));
        when(cashFlowCalculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = cashFlowStateMachineService
                .acquireStateMachine(machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<CashFlowStates, CashFlowEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(CashFlowStates.CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashFlowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.ETL_START)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.ETL_SEND_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_START)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.REVERSED_ETL_START)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.REVERSED_ETL_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.REVERSED_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.REVERSED_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.DATA_COPIED_FROM_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.DATA_COPIED_FROM_STAGED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_FINISHED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.REVERSED_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashFlowStates.CALCULATION_FINISHED)
                .and()
                .build()
                .test();
        // @formatter:on
    }

    @Test
    void testError()
            throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId, CASH_FLOW));
        when(cashFlowCalculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = cashFlowStateMachineService
                .acquireStateMachine(machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<CashFlowStates, CashFlowEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(CashFlowStates.CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.CALCULATION_ERROR_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashFlowStates.CALCULATION_ERROR)
                .and()
                .step()
                    .sendEvent(CashFlowEvents.ETL_ACCEPTED_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashFlowStates.CALCULATION_ERROR)
                .and()
                .build()
                .test();
        // @formatter:on
    }

}
