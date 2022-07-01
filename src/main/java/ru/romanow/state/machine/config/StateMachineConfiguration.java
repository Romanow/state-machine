package ru.romanow.state.machine.config;

import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfiguration
        extends EnumStateMachineConfigurerAdapter<States, Events> {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineConfiguration.class);

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates()
              .initial(States.DATA_PREPARED)
              .end(States.CALCULATION_FINISHED)
              .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        // @formatter:off
        transitions
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

    @Bean
    public ApplicationListener<StateMachineEvent> stateMachineEventApplicationListener() {
        return event -> logger.info(event.toString());
    }

}
