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
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.config.StateMachineConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.CustomStateMachinePersist;
import ru.romanow.state.machine.service.StateMachineService;

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
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private StateMachineService stateMachineService;

    @Test
    void test() throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId));
        when(calculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = stateMachineService
                .acquireStateMachine(CASH_FLOW.value(), machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<States, Events>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(States.CASH_FLOW_CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(States.CASH_FLOW_DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_ETL_START)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_ETL_SEND_TO_DRP)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_ETL_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_ETL_COMPLETED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_CALCULATION_START)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_REVERSED_ETL_START_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_REVERSED_ETL_START)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_REVERSED_ETL_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_REVERSED_ETL_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_REVERSED_ETL_ACCEPTED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_REVERSED_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_REVERSED_COMPLETED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_CALCULATION_FINISHED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CASH_FLOW_CALCULATION_FINISHED)
                .and()
                .step()
                    .sendEvent(Events.CASH_FLOW_REVERSED_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(States.CASH_FLOW_CALCULATION_FINISHED)
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

    @Configuration
    @Import(StateMachineConfiguration.class)
    static class TestConfiguration {

        @Bean
        public CalculationStatusRepository calculationStatusRepository() {
            return mock(CalculationStatusRepository.class);
        }

        @Bean
        public CalculationRepository calculationRepository() {
            return mock(CalculationRepository.class);
        }

        @Bean
        public StateMachineRuntimePersister<States, Events, String> stateMachinePersist() {
            return new CustomStateMachinePersist(calculationRepository(), calculationStatusRepository());
        }

    }

}
