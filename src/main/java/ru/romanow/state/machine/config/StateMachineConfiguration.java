package ru.romanow.state.machine.config;

import java.util.EnumSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import ru.romanow.state.machine.models.CashflowEvents;
import ru.romanow.state.machine.models.CashflowStates;
import ru.romanow.state.machine.service.StateMachineService;
import ru.romanow.state.machine.service.StateMachineServiceImpl;

import static ru.romanow.state.machine.domain.CalculationTypes.CASHFLOW;

@Configuration
@RequiredArgsConstructor
public class StateMachineConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineConfiguration.class);

    private final StateMachineRuntimePersister<CashflowStates, CashflowEvents, String> stateMachinePersist;

    @Bean
    @Autowired
    public StateMachineService stateMachineService(
            Map<String, StateMachineFactory<CashflowStates, CashflowEvents>> stateMachineFactories) {
        return new StateMachineServiceImpl(stateMachinePersist, stateMachineFactories);
    }

    @Bean
    public ApplicationListener<StateMachineEvent> stateMachineEventApplicationListener() {
        return event -> logger.debug(event.toString());
    }

    @Configuration
    @EnableStateMachineFactory(name = CASHFLOW)
    @RequiredArgsConstructor
    class CashflowStateMachineConfiguration
            extends EnumStateMachineConfigurerAdapter<CashflowStates, CashflowEvents> {

        @Override
        public void configure(StateMachineConfigurationConfigurer<CashflowStates, CashflowEvents> config)
                throws Exception {
            // @formatter:off
            config.withConfiguration()
                      .autoStartup(true)
                  .and()
                      .withPersistence()
                      .runtimePersister(stateMachinePersist);
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
}
