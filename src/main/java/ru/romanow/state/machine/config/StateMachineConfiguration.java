package ru.romanow.state.machine.config;

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
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.cashflow.CashFlowCustomStateMachinePersist;
import ru.romanow.state.machine.service.vssdv.VssdvCustomStateMachinePersist;

import static java.util.EnumSet.allOf;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
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
    static class CashFlowStateMachineConfiguration
            extends EnumStateMachineConfigurerAdapter<CashFlowStates, CashFlowEvents> {
        private final CashFlowCustomStateMachinePersist cashFlowStateMachinePersist;

        @Override
        public void configure(StateMachineConfigurationConfigurer<CashFlowStates, CashFlowEvents> config)
                throws Exception {
            // @formatter:off
            config.withConfiguration()
                      .autoStartup(true)
                      .listener(new StateMachineListener<>())
                  .and()
                      .withPersistence()
                      .runtimePersister(cashFlowStateMachinePersist);
            // @formatter:on
        }

        @Override
        public void configure(StateMachineStateConfigurer<CashFlowStates, CashFlowEvents> states)
                throws Exception {
            states.withStates()
                  .initial(CashFlowStates.CALCULATION_STARTED)
                  .end(CashFlowStates.CALCULATION_FINISHED)
                  .end(CashFlowStates.CALCULATION_ERROR)
                  .states(allOf(CashFlowStates.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<CashFlowStates, CashFlowEvents> transitions)
                throws Exception {
            // @formatter:off
            transitions
                .withExternal()
                    .source(CashFlowStates.CALCULATION_STARTED)
                    .target(CashFlowStates.DATA_PREPARED)
                    .event(CashFlowEvents.DATA_PREPARED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.DATA_PREPARED)
                    .target(CashFlowStates.DATA_COPIED_TO_STAGED)
                    .event(CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.DATA_COPIED_TO_STAGED)
                    .target(CashFlowStates.ETL_START)
                    .event(CashFlowEvents.ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.ETL_START)
                    .target(CashFlowStates.ETL_SENT_TO_DRP)
                    .event(CashFlowEvents.ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.ETL_SENT_TO_DRP)
                    .target(CashFlowStates.ETL_ACCEPTED)
                    .event(CashFlowEvents.ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.ETL_ACCEPTED)
                    .target(CashFlowStates.ETL_COMPLETED)
                    .event(CashFlowEvents.ETL_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.ETL_COMPLETED)
                    .target(CashFlowStates.CALCULATION_START)
                    .event(CashFlowEvents.CALCULATION_START_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.CALCULATION_START)
                    .target(CashFlowStates.CALCULATION_SENT_TO_DRP)
                    .event(CashFlowEvents.CALCULATION_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.CALCULATION_SENT_TO_DRP)
                    .target(CashFlowStates.CALCULATION_ACCEPTED)
                    .event(CashFlowEvents.CALCULATION_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.CALCULATION_ACCEPTED)
                    .target(CashFlowStates.CALCULATION_COMPLETED)
                    .event(CashFlowEvents.CALCULATION_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.CALCULATION_COMPLETED)
                    .target(CashFlowStates.REVERSED_ETL_START)
                    .event(CashFlowEvents.REVERSED_ETL_START_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.REVERSED_ETL_START)
                    .target(CashFlowStates.REVERSED_ETL_SENT_TO_DRP)
                    .event(CashFlowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.REVERSED_ETL_SENT_TO_DRP)
                    .target(CashFlowStates.REVERSED_ETL_ACCEPTED)
                    .event(CashFlowEvents.REVERSED_ETL_ACCEPTED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.REVERSED_ETL_ACCEPTED)
                    .target(CashFlowStates.REVERSED_ETL_COMPLETED)
                    .event(CashFlowEvents.REVERSED_ETL_COMPLETED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.REVERSED_ETL_COMPLETED)
                    .target(CashFlowStates.DATA_COPIED_FROM_STAGED)
                    .event(CashFlowEvents.DATA_COPIED_FROM_STAGED_EVENT)
                .and()
                .withExternal()
                    .source(CashFlowStates.DATA_COPIED_FROM_STAGED)
                    .target(CashFlowStates.CALCULATION_FINISHED)
                    .event(CashFlowEvents.CALCULATION_FINISHED_EVENT);
            // @formatter:on

            for (var state : CashFlowStates.values()) {
                transitions
                        .withExternal()
                        .source(state)
                        .target(CashFlowStates.CALCULATION_ERROR)
                        .event(CashFlowEvents.CALCULATION_ERROR_EVENT);
            }
        }
    }

    @Configuration
    @EnableStateMachineFactory(name = VSSDV)
    @RequiredArgsConstructor
    static class VssdvStateMachineConfiguration
            extends EnumStateMachineConfigurerAdapter<VssdvStates, VssdvEvents> {
        private final VssdvCustomStateMachinePersist vssdvStateMachinePersist;

        @Override
        public void configure(StateMachineConfigurationConfigurer<VssdvStates, VssdvEvents> config) throws Exception {
            // @formatter:off
            config.withConfiguration()
                      .autoStartup(true)
                      .listener(new StateMachineListener<>())
                  .and()
                      .withPersistence()
                      .runtimePersister(vssdvStateMachinePersist);
            // @formatter:on
        }

        @Override
        public void configure(StateMachineStateConfigurer<VssdvStates, VssdvEvents> states) throws Exception {
            // @formatter:off
            states.withStates()
                      .region("VSSDV")
                      .initial(VssdvStates.CALCULATION_STARTED)
                      .fork(VssdvStates.CALCULATION_STARTED)
                      .join(VssdvStates.VSSDV_JOIN_STATE)
                      .states(allOf(VssdvStates.class)
                                      .stream()
                                      .filter(s -> s.name().startsWith("VSSDV_"))
                                      .collect(toSet()))
                      .end(VssdvStates.VSSDV_CALCULATION_FINISHED)
                      .end(VssdvStates.CALCULATION_ERROR)
                  .and()
                  .withStates()
                      .region("Var Model")
                      .parent(VssdvStates.CALCULATION_STARTED)
                      .initial(VssdvStates.VAR_MODEL_CALCULATION_STARTED)
                      .end(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                      .states(allOf(VssdvStates.class)
                                      .stream()
                                      .filter(s -> s.name().startsWith("VAR_MODEL_"))
                                      .collect(toSet()))
                  .and()
                  .withStates()
                      .region("Black Model")
                      .parent(VssdvStates.CALCULATION_STARTED)
                      .initial(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                      .end(VssdvStates.BLACK_MODEL_CALCULATION_FINISHED)
                      .states(allOf(VssdvStates.class)
                                      .stream()
                                      .filter(s -> s.name().startsWith("BLACK_MODEL_"))
                                      .collect(toSet()));
            // @formatter:on
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<VssdvStates, VssdvEvents> transitions) throws Exception {
            // @formatter:off
            // region Var Model
            transitions
                    .withFork()
                        .source(VssdvStates.CALCULATION_STARTED)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_STARTED)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_STARTED)
                        .target(VssdvStates.VAR_MODEL_DATA_PREPARED)
                        .event(VssdvEvents.VAR_MODEL_DATA_PREPARED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_DATA_PREPARED)
                        .target(VssdvStates.VAR_MODEL_DATA_COPIED_TO_STAGED)
                        .event(VssdvEvents.VAR_MODEL_DATA_COPIED_TO_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_DATA_COPIED_TO_STAGED)
                        .target(VssdvStates.VAR_MODEL_ETL_START)
                        .event(VssdvEvents.VAR_MODEL_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_ETL_START)
                        .target(VssdvStates.VAR_MODEL_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.VAR_MODEL_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_ETL_SENT_TO_DRP)
                        .target(VssdvStates.VAR_MODEL_ETL_ACCEPTED)
                        .event(VssdvEvents.VAR_MODEL_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_ETL_ACCEPTED)
                        .target(VssdvStates.VAR_MODEL_ETL_COMPLETED)
                        .event(VssdvEvents.VAR_MODEL_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_ETL_COMPLETED)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_START)
                        .event(VssdvEvents.VAR_MODEL_CALCULATION_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_START)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_SENT_TO_DRP)
                        .event(VssdvEvents.VAR_MODEL_CALCULATION_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_SENT_TO_DRP)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_ACCEPTED)
                        .event(VssdvEvents.VAR_MODEL_CALCULATION_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_ACCEPTED)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                        .event(VssdvEvents.VAR_MODEL_CALCULATION_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_COMPLETED)
                        .target(VssdvStates.VAR_MODEL_REVERSED_ETL_START)
                        .event(VssdvEvents.VAR_MODEL_REVERSED_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_REVERSED_ETL_START)
                        .target(VssdvStates.VAR_MODEL_REVERSED_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.VAR_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_REVERSED_ETL_SENT_TO_DRP)
                        .target(VssdvStates.VAR_MODEL_REVERSED_ETL_ACCEPTED)
                        .event(VssdvEvents.VAR_MODEL_REVERSED_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_REVERSED_ETL_ACCEPTED)
                        .target(VssdvStates.VAR_MODEL_REVERSED_ETL_COMPLETED)
                        .event(VssdvEvents.VAR_MODEL_REVERSED_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_REVERSED_ETL_COMPLETED)
                        .target(VssdvStates.VAR_MODEL_DATA_COPIED_FROM_STAGED)
                        .event(VssdvEvents.VAR_MODEL_DATA_COPIED_FROM_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VAR_MODEL_DATA_COPIED_FROM_STAGED)
                        .target(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                        .event(VssdvEvents.VAR_MODEL_CALCULATION_FINISHED_EVENT);
            // endregion
            
            // region Black Model
            transitions
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_STARTED)
                        .target(VssdvStates.BLACK_MODEL_DATA_PREPARED)
                        .event(VssdvEvents.BLACK_MODEL_DATA_PREPARED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_DATA_PREPARED)
                        .target(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                        .event(VssdvEvents.BLACK_MODEL_DATA_COPIED_TO_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_DATA_COPIED_TO_STAGED)
                        .target(VssdvStates.BLACK_MODEL_ETL_START)
                        .event(VssdvEvents.BLACK_MODEL_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_ETL_START)
                        .target(VssdvStates.BLACK_MODEL_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.BLACK_MODEL_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_ETL_SENT_TO_DRP)
                        .target(VssdvStates.BLACK_MODEL_ETL_ACCEPTED)
                        .event(VssdvEvents.BLACK_MODEL_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_ETL_ACCEPTED)
                        .target(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                        .event(VssdvEvents.BLACK_MODEL_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_ETL_COMPLETED)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_START)
                        .event(VssdvEvents.BLACK_MODEL_CALCULATION_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_START)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_SENT_TO_DRP)
                        .event(VssdvEvents.BLACK_MODEL_CALCULATION_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_SENT_TO_DRP)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_ACCEPTED)
                        .event(VssdvEvents.BLACK_MODEL_CALCULATION_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_ACCEPTED)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_COMPLETED)
                        .event(VssdvEvents.BLACK_MODEL_CALCULATION_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_COMPLETED)
                        .target(VssdvStates.BLACK_MODEL_REVERSED_ETL_START)
                        .event(VssdvEvents.BLACK_MODEL_REVERSED_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_REVERSED_ETL_START)
                        .target(VssdvStates.BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP)
                        .target(VssdvStates.BLACK_MODEL_REVERSED_ETL_ACCEPTED)
                        .event(VssdvEvents.BLACK_MODEL_REVERSED_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_REVERSED_ETL_ACCEPTED)
                        .target(VssdvStates.BLACK_MODEL_REVERSED_ETL_COMPLETED)
                        .event(VssdvEvents.BLACK_MODEL_REVERSED_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_REVERSED_ETL_COMPLETED)
                        .target(VssdvStates.BLACK_MODEL_DATA_COPIED_FROM_STAGED)
                        .event(VssdvEvents.BLACK_MODEL_DATA_COPIED_FROM_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.BLACK_MODEL_DATA_COPIED_FROM_STAGED)
                        .target(VssdvStates.BLACK_MODEL_CALCULATION_FINISHED)
                        .event(VssdvEvents.BLACK_MODEL_CALCULATION_FINISHED_EVENT);
            // endregion

            // region Black Model
            transitions
                    .withJoin()
                        .source(VssdvStates.VAR_MODEL_CALCULATION_FINISHED)
                        .source(VssdvStates.BLACK_MODEL_CALCULATION_FINISHED)
                        .target(VssdvStates.VSSDV_JOIN_STATE)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_JOIN_STATE)
                        .target(VssdvStates.VSSDV_CALCULATION_STARTED)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_CALCULATION_STARTED)
                        .target(VssdvStates.VSSDV_DATA_PREPARED)
                        .event(VssdvEvents.VSSDV_DATA_PREPARED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_DATA_PREPARED)
                        .target(VssdvStates.VSSDV_DATA_COPIED_TO_STAGED)
                        .event(VssdvEvents.VSSDV_DATA_COPIED_TO_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_DATA_COPIED_TO_STAGED)
                        .target(VssdvStates.VSSDV_ETL_START)
                        .event(VssdvEvents.VSSDV_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_ETL_START)
                        .target(VssdvStates.VSSDV_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.VSSDV_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_ETL_SENT_TO_DRP)
                        .target(VssdvStates.VSSDV_ETL_ACCEPTED)
                        .event(VssdvEvents.VSSDV_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_ETL_ACCEPTED)
                        .target(VssdvStates.VSSDV_ETL_COMPLETED)
                        .event(VssdvEvents.VSSDV_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_ETL_COMPLETED)
                        .target(VssdvStates.VSSDV_CALCULATION_START)
                        .event(VssdvEvents.VSSDV_CALCULATION_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_CALCULATION_START)
                        .target(VssdvStates.VSSDV_CALCULATION_SENT_TO_DRP)
                        .event(VssdvEvents.VSSDV_CALCULATION_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_CALCULATION_SENT_TO_DRP)
                        .target(VssdvStates.VSSDV_CALCULATION_ACCEPTED)
                        .event(VssdvEvents.VSSDV_CALCULATION_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_CALCULATION_ACCEPTED)
                        .target(VssdvStates.VSSDV_CALCULATION_COMPLETED)
                        .event(VssdvEvents.VSSDV_CALCULATION_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_CALCULATION_COMPLETED)
                        .target(VssdvStates.VSSDV_REVERSED_ETL_START)
                        .event(VssdvEvents.VSSDV_REVERSED_ETL_START_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_REVERSED_ETL_START)
                        .target(VssdvStates.VSSDV_REVERSED_ETL_SENT_TO_DRP)
                        .event(VssdvEvents.VSSDV_REVERSED_ETL_SENT_TO_DRP_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_REVERSED_ETL_SENT_TO_DRP)
                        .target(VssdvStates.VSSDV_REVERSED_ETL_ACCEPTED)
                        .event(VssdvEvents.VSSDV_REVERSED_ETL_ACCEPTED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_REVERSED_ETL_ACCEPTED)
                        .target(VssdvStates.VSSDV_REVERSED_ETL_COMPLETED)
                        .event(VssdvEvents.VSSDV_REVERSED_ETL_COMPLETED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_REVERSED_ETL_COMPLETED)
                        .target(VssdvStates.VSSDV_DATA_COPIED_FROM_STAGED)
                        .event(VssdvEvents.VSSDV_DATA_COPIED_FROM_STAGED_EVENT)
                    .and()
                    .withExternal()
                        .source(VssdvStates.VSSDV_DATA_COPIED_FROM_STAGED)
                        .target(VssdvStates.VSSDV_CALCULATION_FINISHED)
                        .event(VssdvEvents.VSSDV_CALCULATION_FINISHED_EVENT);
            // endregion
            // @formatter:on

            for (var state : VssdvStates.values()) {
                transitions.withExternal()
                           .source(state)
                           .target(VssdvStates.CALCULATION_ERROR)
                           .event(VssdvEvents.CALCULATION_ERROR_EVENT);
            }
        }
    }

    private static class StateMachineListener<States extends Enum<States>, Events extends Enum<Events>>
            extends StateMachineListenerAdapter<States, Events> {
        @Override
        public void stateChanged(State<States, Events> from, State<States, Events> to) {
            logger.info("State changed from {} to: {}", nonNull(from) ? from.getIds() : "empty", to.getIds());
        }
    }
}
