package ru.romanow.state.machine;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.CustomStateMachineService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

@ActiveProfiles("test")
@SpringBootTest
public class StateMachineStatusTest {

    @MockBean
    private CalculationRepository calculationRepository;

    @MockBean
    private CalculationStatusRepository calculationStatusRepository;

    @Autowired
    private CustomStateMachineService stateMachineService;

    @Test
    void test() throws Exception {
        var machineId = UUID.randomUUID();
        when(calculationRepository.findByUid(machineId))
                .thenReturn(buildCalculation(machineId));
        when(calculationStatusRepository.getCalculationLastState(eq(machineId), any(Pageable.class)))
                .thenReturn(List.of());

        var stateMachine = stateMachineService
                .acquireStateMachine(machineId.toString());

        // @formatter:off
        StateMachineTestPlanBuilder
                .<States, Events>builder()
                .defaultAwaitTime(1)
                .stateMachine(stateMachine)
                .step()
                    .expectState(States.CALCULATION_STARTED)
                .and()
                .step()
                    .sendEvent(Events.DATA_PREPARED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_COMPLETED_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(States.DATA_PREPARED)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_START_WITHOUT_ETL_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CALCULATION_START)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_SENT_TO_DRP_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CALCULATION_SENT_TO_DRP)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_ACCEPTED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CALCULATION_ACCEPTED)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_COMPLETED_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CALCULATION_COMPLETED)
                .and()
                .step()
                    .sendEvent(Events.CALCULATION_FINISHED_WITHOUT_REVERSE_ETL_EVENT)
                    .expectStateChanged(1)
                    .expectState(States.CALCULATION_FINISHED)
                .and()
                .step()
                    .sendEvent(Events.REVERSED_ETL_START_EVENT)
                    .expectEventNotAccepted(1)
                    .expectState(States.CALCULATION_FINISHED)
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

}
