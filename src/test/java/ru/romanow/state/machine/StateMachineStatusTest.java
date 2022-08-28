package ru.romanow.state.machine;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.statemachine.config.StateMachineFactory;
import ru.romanow.state.machine.config.StateMachineConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.repostitory.VssdvCalculationStatusRepository;
import ru.romanow.state.machine.service.cashflow.CashFlowCustomStateMachinePersist;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;
import ru.romanow.state.machine.service.vssdv.VssdvCustomStateMachinePersist;
import ru.romanow.state.machine.service.vssdv.VssdvStateMachineService;

import static org.mockito.Mockito.mock;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

public class StateMachineStatusTest {

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
                StateMachineFactory<CashFlowStates, CashFlowEvents> stateMachineFactory) {
            return new CashFlowStateMachineService(cashFlowCustomStateMachinePersist(), stateMachineFactory);
        }

        @Bean
        public VssdvCustomStateMachinePersist vssdvCustomStateMachinePersist() {
            return mock(VssdvCustomStateMachinePersist.class);
        }

        @Bean
        public VssdvStateMachineService vssdvStateMachineService(
                StateMachineFactory<VssdvStates, VssdvEvents> stateMachineFactory) {
            return new VssdvStateMachineService(vssdvCustomStateMachinePersist(), stateMachineFactory);
        }

    }

}
