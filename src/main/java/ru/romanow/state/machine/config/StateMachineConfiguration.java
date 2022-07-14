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
                  .initial(States.CALCULATION_STARTED)
                  .end(States.CALCULATION_FINISHED)
                  .states(EnumSet.allOf(States.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
                throws Exception {
            // @formatter:off
        transitions
                .withExternal()
                    .source(States.CALCULATION_STARTED)
                    .target(States.DATA_PREPARED)
                    .event(Events.DATA_PREPARED_EVENT)
                .and()
                .withExternal()
                    .source(States.DATA_PREPARED)
                    .target(States.ETL_START)
                    .event(Events.ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(States.ETL_START)
                    .target(States.ETL_SEND_TO_DRP)
                    .event(Events.ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.ETL_SEND_TO_DRP)
                    .target(States.ETL_ACCEPTED)
                    .event(Events.ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.ETL_ACCEPTED)
                    .target(States.ETL_COMPLETED)
                    .event(Events.ETL_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.ETL_COMPLETED)
                    .target(States.CALCULATION_START)
                    .event(Events.CALCULATION_START_EVENT)
                .and()
                .withExternal()
                    .source(States.DATA_PREPARED)
                    .target(States.CALCULATION_START)
                    .event(Events.CALCULATION_START_WITHOUT_ETL_EVENT)
                .and()
                .withExternal()
                    .source(States.CALCULATION_START)
                    .target(States.CALCULATION_SENT_TO_DRP)
                    .event(Events.CALCULATION_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.CALCULATION_SENT_TO_DRP)
                    .target(States.CALCULATION_ACCEPTED)
                    .event(Events.CALCULATION_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.CALCULATION_ACCEPTED)
                    .target(States.CALCULATION_COMPLETED)
                    .event(Events.CALCULATION_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.CALCULATION_COMPLETED)
                    .target(States.REVERSED_ETL_START)
                    .event(Events.REVERSED_ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(States.CALCULATION_COMPLETED)
                    .target(States.CALCULATION_FINISHED)
                    .event(Events.CALCULATION_FINISHED_WITHOUT_REVERSE_ETL_EVENT)
                .and()
                .withExternal()
                    .source(States.REVERSED_ETL_START)
                    .target(States.REVERSED_ETL_SENT_TO_DRP)
                    .event(Events.REVERSED_ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(States.REVERSED_ETL_SENT_TO_DRP)
                    .target(States.REVERSED_ETL_ACCEPTED)
                    .event(Events.REVERSED_ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(States.REVERSED_ETL_ACCEPTED)
                    .target(States.REVERSED_COMPLETED)
                    .event(Events.REVERSED_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(States.REVERSED_COMPLETED)
                    .target(States.CALCULATION_FINISHED)
                    .event(Events.CALCULATION_FINISHED_EVENT);
        // @formatter:on
        }
    }
}
