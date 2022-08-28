package ru.romanow.state.machine;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.config.StateMachineConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.cashflow.CashflowEvents;
import ru.romanow.state.machine.models.cashflow.CashflowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.repostitory.VssdvCalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowCustomStateMachinePersist;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;
import ru.romanow.state.machine.service.vssdv.VssdvCustomStateMachinePersist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

@ActiveProfiles("test")
@SpringBootTest
public class StateMachineStatusTest {

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CashFlowCalculationStatusRepository cashFlowCalculationStatusRepository;

    @Autowired
    private CashFlowStateMachineService cashFlowStateMachineService;

    @Test
    void testSuccess()
            throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId));
        when(cashFlowCalculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = cashFlowStateMachineService
                .acquireStateMachine(CASH_FLOW.value(), machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<CashflowStates, CashflowEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(CashflowStates.CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashflowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.ETL_START)
                .and()
                .step()
                    .sendEvent(CashflowEvents.ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.ETL_SEND_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashflowEvents.ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_START)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.REVERSED_ETL_START)
                .and()
                .step()
                    .sendEvent(CashflowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.REVERSED_ETL_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(CashflowEvents.REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.REVERSED_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.REVERSED_COMPLETED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.DATA_COPIED_FROM_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.DATA_COPIED_FROM_STAGED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_FINISHED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.REVERSED_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashflowStates.CALCULATION_FINISHED)
                .and()
                .build()
                .test();
        // @formatter:on
    }

    @NotNull
    private Calculation buildCalculation(@NotNull UUID uid) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(CalculationType.CASH_FLOW)
                .setDescription(randomAlphabetic(20));
    }

    @Test
    void testError()
            throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId));
        when(cashFlowCalculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = cashFlowStateMachineService
                .acquireStateMachine(CASH_FLOW.value(), machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<CashflowStates, CashflowEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(CashflowStates.CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(CashflowEvents.CALCULATION_ERROR_EVENT)
                    .expectStateChanged(1)
                    .expectState(CashflowStates.CALCULATION_ERROR)
                .and()
                .step()
                    .sendEvent(CashflowEvents.ETL_ACCEPTED_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(CashflowStates.CALCULATION_ERROR)
                .and()
                .build()
                .test();
        // @formatter:on
    }

    @Configuration
    @Import(StateMachineConfiguration.class)
    static class TestConfiguration {

        @Bean
        public CashFlowCalculationStatusRepository cashFlowCalculationStatusRepository() {
            return mock(CashFlowCalculationStatusRepository.class);
        }

        @Bean
        public VssdvCalculationStatusRepository vssdvCalculationStatusRepository() {
            return mock(VssdvCalculationStatusRepository.class);
        }

        @Bean
        public CalculationRepository calculationRepository() {
            return mock(CalculationRepository.class);
        }

        @Bean
        public CashFlowCustomStateMachinePersist cashFlowCustomStateMachinePersist() {
            return new CashFlowCustomStateMachinePersist(calculationRepository(),
                                                         cashFlowCalculationStatusRepository());
        }

        @Bean
        public CashFlowStateMachineService cashFlowStateMachineService(
                StateMachineFactory<CashflowStates, CashflowEvents> stateMachineFactory) {
            return new CashFlowStateMachineService(cashFlowCustomStateMachinePersist(), stateMachineFactory);
        }

        @Bean
        public VssdvCustomStateMachinePersist vssdvCustomStateMachinePersist() {
            return mock(VssdvCustomStateMachinePersist.class);
        }

    }

}
