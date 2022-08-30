package ru.romanow.state.machine.statuses;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowCustomStateMachinePersist;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;
import ru.romanow.state.machine.service.vssdv.VssdvCustomStateMachinePersist;
import ru.romanow.state.machine.service.vssdv.VssdvStateMachineService;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.romanow.state.machine.domain.enums.CalculationType.CASH_FLOW;

@ActiveProfiles("test")
@SpringBootTest
class CashFlowStateMachineStatusTest {

    @Autowired
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private CashFlowStateMachineService cashFlowStateMachineService;

    @Autowired
    protected CalculationRepository calculationRepository;

    @Test
    void testSuccess()
            throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId));
        when(calculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
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
                .thenReturn(buildCalculation(machineId));
        when(calculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
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

    @NotNull
    private Calculation buildCalculation(@NotNull UUID uid) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(CASH_FLOW)
                .setDescription(randomAlphabetic(20));
    }

    @Configuration
    @Import(StateMachineConfiguration.class)
    static class TestConfiguration {

        @MockBean
        private CalculationRepository calculationRepository;

        @MockBean
        private CalculationStatusRepository calculationStatusRepository;

        @Bean
        public CashFlowCustomStateMachinePersist cashFlowCustomStateMachinePersist() {
            return new CashFlowCustomStateMachinePersist(calculationRepository, calculationStatusRepository);
        }

        @Bean
        @Autowired
        public CashFlowStateMachineService cashFlowStateMachineService(
                StateMachineFactory<CashFlowStates, CashFlowEvents> stateMachineFactory
        ) {
            return new CashFlowStateMachineService(cashFlowCustomStateMachinePersist(), stateMachineFactory);
        }

        @Bean
        public VssdvCustomStateMachinePersist vssdvCustomStateMachinePersist() {
            return mock(VssdvCustomStateMachinePersist.class);
        }

        @Bean
        public VssdvStateMachineService vssdvStateMachineService() {
            return mock(VssdvStateMachineService.class);
        }

    }

}
