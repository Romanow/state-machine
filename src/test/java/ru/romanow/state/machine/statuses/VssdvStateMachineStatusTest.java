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
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
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
import static ru.romanow.state.machine.domain.enums.CalculationType.VSSDV;

@ActiveProfiles("test")
@SpringBootTest
class VssdvStateMachineStatusTest {

    @Autowired
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private VssdvStateMachineService cashFlowStateMachineService;

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
                .<VssdvStates, VssdvEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_STARTED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_PREPARED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_PREPARED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_COPIED_TO_STAGED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_COPIED_TO_STAGED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_ETL_START)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_ETL_SEND_TO_DRP)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_ETL_ACCEPTED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_ETL_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_START)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_SENT_TO_DRP)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_ACCEPTED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_SEND_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_REVERSED_ETL_START)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_REVERSED_ETL_SENT_TO_DRP)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_REVERSED_ETL_ACCEPTED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_REVERSED_COMPLETED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_DATA_COPIED_FROM_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_COPIED_FROM_STAGED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(0)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_REVERSED_ETL_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_REVERSED_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_REVERSED_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_DATA_COPIED_FROM_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_COPIED_FROM_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(2)
                    .expectState(VssdvStates.VSSDV_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_DATA_COPIED_TO_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(VssdvStates.VSSDV_DATA_COPIED_TO_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_ETL_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_ETL_SEND_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_REVERSED_ETL_START)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_REVERSED_ETL_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_REVERSED_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_REVERSED_COMPLETED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_DATA_COPIED_FROM_STAGED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_DATA_COPIED_FROM_STAGED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_FINISHED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VSSDV_REVERSED_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(VssdvStates.VSSDV_CALCULATION_FINISHED)
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
                .<VssdvStates, VssdvEvents>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_CALCULATION_STARTED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_PREPARED)
                    .expectState(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.BLACK_MODEL_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_STARTED)
                    .expectState(VssdvStates.VAR_MODEL_DATA_PREPARED)
                    .expectState(VssdvStates.BLACK_MODEL_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(VssdvEvents.CALCULATION_ERROR_EVENT)
                    .expectStateChanged(1)
                    .expectState(VssdvStates.CALCULATION_ERROR)
                .and()
                .step()
                    .sendEvent(VssdvEvents.VAR_MODEL_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(VssdvStates.CALCULATION_ERROR)
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
                .setType(VSSDV)
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
            return mock(CashFlowCustomStateMachinePersist.class);
        }

        @Bean
        public CashFlowStateMachineService cashFlowStateMachineService() {
            return mock(CashFlowStateMachineService.class);
        }

        @Bean
        public VssdvCustomStateMachinePersist vssdvCustomStateMachinePersist() {
            return new VssdvCustomStateMachinePersist(calculationRepository, calculationStatusRepository);
        }

        @Bean
        @Autowired
        public VssdvStateMachineService vssdvStateMachineService(
                StateMachineFactory<VssdvStates, VssdvEvents> stateMachineFactory
        ) {
            return new VssdvStateMachineService(vssdvCustomStateMachinePersist(), stateMachineFactory);
        }

    }

}
