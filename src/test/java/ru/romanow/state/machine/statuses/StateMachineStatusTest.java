package ru.romanow.state.machine.statuses;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.config.StateMachineConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowCustomStateMachinePersist;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;
import ru.romanow.state.machine.service.vssdv.VssdvCustomStateMachinePersist;
import ru.romanow.state.machine.service.vssdv.VssdvStateMachineService;
import ru.romanow.state.machine.statuses.StateMachineStatusTest.StateMachineStatusTestConfiguration;

import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

@ActiveProfiles("test")
@SpringBootTest(classes = StateMachineStatusTestConfiguration.class)
class StateMachineStatusTest {

    @Autowired
    protected CalculationRepository calculationRepository;

    @NotNull
    protected Calculation buildCalculation(@NotNull UUID uid, @NotNull CalculationType calculationType) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(calculationType)
                .setDescription(randomAlphabetic(20));
    }

    @Configuration
    @Import(StateMachineConfiguration.class)
    static class StateMachineStatusTestConfiguration {

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
