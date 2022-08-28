package ru.romanow.state.machine.config;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import ru.romanow.state.machine.domain.CashFlowCalculationStatus;
import ru.romanow.state.machine.domain.VssdvCalculationStatus;
import ru.romanow.state.machine.models.cashflow.CashflowEvents;
import ru.romanow.state.machine.models.cashflow.CashflowStates;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.repostitory.VssdvCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;

import static ru.romanow.state.machine.domain.CalculationTypes.CASHFLOW;
import static ru.romanow.state.machine.domain.CalculationTypes.VSSDV;

@Configuration
public class StateMachineConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineConfiguration.class);

    @Bean
    public ApplicationListener<StateMachineEvent> stateMachineEventApplicationListener() {
        return event -> logger.debug(event.toString());
    }

    @Configuration
    @EnableStateMachineFactory(name = CASHFLOW)
    @RequiredArgsConstructor
    static class CashflowStateMachineConfiguration
            extends EnumStateMachineConfigurerAdapter<CashflowStates, CashflowEvents> {
        private final BaseCustomStateMachinePersist<CashflowStates, CashflowEvents, CashFlowCalculationStatus, CashFlowCalculationStatusRepository> cashFlowStateMachinePersist;

        @Override
        public void configure(StateMachineConfigurationConfigurer<CashflowStates, CashflowEvents> config)
                throws Exception {
            // @formatter:off
            config.withConfiguration()
                      .autoStartup(true)
                  .and()
                      .withPersistence()
                      .runtimePersister(cashFlowStateMachinePersist);
            // @formatter:on
        }

        @Override
        public void configure(StateMachineStateConfigurer<CashflowStates, CashflowEvents> states)
                throws Exception {
            states.withStates()
                  .initial(CashflowStates.CALCULATION_STARTED)
                  .end(CashflowStates.CALCULATION_FINISHED)
                  .end(CashflowStates.CALCULATION_ERROR)
                  .states(EnumSet.allOf(CashflowStates.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<CashflowStates, CashflowEvents> transitions)
                throws Exception {
            // @formatter:off
            transitions
                .withExternal()
                    .source(CashflowStates.CALCULATION_STARTED)
                    .target(CashflowStates.DATA_PREPARED)
                    .event(CashflowEvents.DATA_PREPARED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.DATA_PREPARED)
                    .target(CashflowStates.DATA_COPIED_TO_STAGED)
                    .event(CashflowEvents.DATA_COPIED_TO_STAGED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.DATA_COPIED_TO_STAGED)
                    .target(CashflowStates.ETL_START)
                    .event(CashflowEvents.ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.ETL_START)
                    .target(CashflowStates.ETL_SEND_TO_DRP)
                    .event(CashflowEvents.ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.ETL_SEND_TO_DRP)
                    .target(CashflowStates.ETL_ACCEPTED)
                    .event(CashflowEvents.ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.ETL_ACCEPTED)
                    .target(CashflowStates.ETL_COMPLETED)
                    .event(CashflowEvents.ETL_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.ETL_COMPLETED)
                    .target(CashflowStates.CALCULATION_START)
                    .event(CashflowEvents.CALCULATION_START_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.CALCULATION_START)
                    .target(CashflowStates.CALCULATION_SENT_TO_DRP)
                    .event(CashflowEvents.CALCULATION_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.CALCULATION_SENT_TO_DRP)
                    .target(CashflowStates.CALCULATION_ACCEPTED)
                    .event(CashflowEvents.CALCULATION_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.CALCULATION_ACCEPTED)
                    .target(CashflowStates.CALCULATION_COMPLETED)
                    .event(CashflowEvents.CALCULATION_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.CALCULATION_COMPLETED)
                    .target(CashflowStates.REVERSED_ETL_START)
                    .event(CashflowEvents.REVERSED_ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.REVERSED_ETL_START)
                    .target(CashflowStates.REVERSED_ETL_SENT_TO_DRP)
                    .event(CashflowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.REVERSED_ETL_SENT_TO_DRP)
                    .target(CashflowStates.REVERSED_ETL_ACCEPTED)
                    .event(CashflowEvents.REVERSED_ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.REVERSED_ETL_ACCEPTED)
                    .target(CashflowStates.REVERSED_COMPLETED)
                    .event(CashflowEvents.REVERSED_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.REVERSED_COMPLETED)
                    .target(CashflowStates.DATA_COPIED_FROM_STAGED)
                    .event(CashflowEvents.DATA_COPIED_FROM_STAGED_EVENT)
                .and()
                .withExternal()
                    .source(CashflowStates.DATA_COPIED_FROM_STAGED)
                    .target(CashflowStates.CALCULATION_FINISHED)
                    .event(CashflowEvents.CALCULATION_FINISHED_EVENT);
            // @formatter:on

            for (var state : CashflowStates.values()) {
                transitions
                        .withExternal()
                        .source(state)
                        .target(CashflowStates.CALCULATION_ERROR)
                        .event(CashflowEvents.CALCULATION_ERROR_EVENT);
            }
        }
    }

    @Configuration
    @EnableStateMachineFactory(name = VSSDV)
    @RequiredArgsConstructor
    static class VssdvStateMachineConfiguration
            extends EnumStateMachineConfigurerAdapter<VssdvStates, VssdvEvents> {
        private final BaseCustomStateMachinePersist<VssdvStates, VssdvEvents, VssdvCalculationStatus, VssdvCalculationStatusRepository> vssdvStateMachinePersist;

        @Override
        public void configure(StateMachineConfigurationConfigurer<VssdvStates, VssdvEvents> config)
                throws Exception {
            // @formatter:off
            config.withConfiguration()
                  .autoStartup(true)
                  .and()
                  .withPersistence()
                  .runtimePersister(vssdvStateMachinePersist);
            // @formatter:on
        }

        @Override
        public void configure(StateMachineStateConfigurer<VssdvStates, VssdvEvents> states)
                throws Exception {
            states.withStates()
                  .initial(VssdvStates.CALCULATION_STARTED)
                  .end(VssdvStates.VSSDV_CALCULATION_FINISHED)
                  .end(VssdvStates.CALCULATION_ERROR)
                  .states(EnumSet.allOf(VssdvStates.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<VssdvStates, VssdvEvents> transitions)
                throws Exception {
            // @formatter:off

            // @formatter:on

            for (var state : VssdvStates.values()) {
                transitions
                        .withExternal()
                        .source(state)
                        .target(VssdvStates.CALCULATION_ERROR)
                        .event(VssdvEvents.CALCULATION_ERROR_EVENT);
            }
        }
    }
}
