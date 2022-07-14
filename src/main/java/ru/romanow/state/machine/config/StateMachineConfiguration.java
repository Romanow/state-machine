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
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.service.StateMachineService;
import ru.romanow.state.machine.service.StateMachineServiceImpl;

import static ru.romanow.state.machine.domain.CalculationTypes.CASHFLOW;

@Configuration
@RequiredArgsConstructor
public class StateMachineConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineConfiguration.class);

    private final StateMachineRuntimePersister<States, Events, String> stateMachinePersist;

    @Bean
    @Autowired
    public StateMachineService stateMachineService(
            Map<String, StateMachineFactory<States, Events>> stateMachineFactories) {
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
            extends EnumStateMachineConfigurerAdapter<States, Events> {

        @Override
        public void configure(StateMachineConfigurationConfigurer<States, Events> config)
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
        public void configure(StateMachineStateConfigurer<States, Events> states)
                throws Exception {
            states.withStates()
                  .initial(States.CASH_FLOW_CALCULATION_STARTED)
                  .end(States.CASH_FLOW_CALCULATION_FINISHED)
                  .states(EnumSet.allOf(States.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
                throws Exception {
            // @formatter:off
        transitions
                .withExternal()
                    .source(States.CASH_FLOW_CALCULATION_STARTED)
                    .target(States.CASH_FLOW_DATA_PREPARED)
                    .event(Events.CASH_FLOW_DATA_PREPARED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_DATA_PREPARED)
                    .target(States.CASH_FLOW_ETL_START)
                    .event(Events.CASH_FLOW_ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_ETL_START)
                    .target(States.CASH_FLOW_ETL_SEND_TO_DRP)
                    .event(Events.CASH_FLOW_ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_ETL_SEND_TO_DRP)
                    .target(States.CASH_FLOW_ETL_ACCEPTED)
                    .event(Events.CASH_FLOW_ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_ETL_ACCEPTED)
                    .target(States.CASH_FLOW_ETL_COMPLETED)
                    .event(Events.CASH_FLOW_ETL_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_ETL_COMPLETED)
                    .target(States.CASH_FLOW_CALCULATION_START)
                    .event(Events.CASH_FLOW_CALCULATION_START_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_CALCULATION_START)
                    .target(States.CASH_FLOW_CALCULATION_SENT_TO_DRP)
                    .event(Events.CASH_FLOW_CALCULATION_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_CALCULATION_SENT_TO_DRP)
                    .target(States.CASH_FLOW_CALCULATION_ACCEPTED)
                    .event(Events.CASH_FLOW_CALCULATION_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_CALCULATION_ACCEPTED)
                    .target(States.CASH_FLOW_CALCULATION_COMPLETED)
                    .event(Events.CASH_FLOW_CALCULATION_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_CALCULATION_COMPLETED)
                    .target(States.CASH_FLOW_REVERSED_ETL_START)
                    .event(Events.CASH_FLOW_REVERSED_ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_REVERSED_ETL_START)
                    .target(States.CASH_FLOW_REVERSED_ETL_SENT_TO_DRP)
                    .event(Events.CASH_FLOW_REVERSED_ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_REVERSED_ETL_SENT_TO_DRP)
                    .target(States.CASH_FLOW_REVERSED_ETL_ACCEPTED)
                    .event(Events.CASH_FLOW_REVERSED_ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_REVERSED_ETL_ACCEPTED)
                    .target(States.CASH_FLOW_REVERSED_COMPLETED)
                    .event(Events.CASH_FLOW_REVERSED_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.CASH_FLOW_REVERSED_COMPLETED)
                    .target(States.CASH_FLOW_CALCULATION_FINISHED)
                    .event(Events.CASH_FLOW_CALCULATION_FINISHED_EVENT);
        // @formatter:on
        }
    }
}
