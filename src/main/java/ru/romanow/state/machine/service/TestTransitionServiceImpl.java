package ru.romanow.state.machine.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.cashflow.CashFlowStateMachineService;
import ru.romanow.state.machine.service.vssdv.VssdvStateMachineService;

import static java.lang.Integer.toHexString;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Mono.just;

@Service
public class TestTransitionServiceImpl
        implements TestTransitionService {

    private static final Logger logger = getLogger(TestTransitionServiceImpl.class);

    private static final Map<CashFlowStates, CashFlowEvents> CASH_FLOW_EVENTS = new HashMap<>() {
        {
            put(CashFlowStates.CALCULATION_STARTED, CashFlowEvents.DATA_PREPARED_EVENT);
            put(CashFlowStates.DATA_PREPARED, CashFlowEvents.DATA_COPIED_TO_STAGED_EVENT);
            put(CashFlowStates.DATA_COPIED_TO_STAGED, CashFlowEvents.ETL_START_EVENT);

            put(CashFlowStates.ETL_START, CashFlowEvents.ETL_SENT_TO_DRP_EVENT);
            put(CashFlowStates.ETL_SENT_TO_DRP, CashFlowEvents.ETL_ACCEPTED_EVENT);
            put(CashFlowStates.ETL_ACCEPTED, CashFlowEvents.ETL_COMPLETED_EVENT);
            put(CashFlowStates.ETL_COMPLETED, CashFlowEvents.CALCULATION_START_EVENT);

            put(CashFlowStates.CALCULATION_START, CashFlowEvents.CALCULATION_SENT_TO_DRP_EVENT);
            put(CashFlowStates.CALCULATION_SENT_TO_DRP, CashFlowEvents.CALCULATION_ACCEPTED_EVENT);
            put(CashFlowStates.CALCULATION_ACCEPTED, CashFlowEvents.CALCULATION_COMPLETED_EVENT);
            put(CashFlowStates.CALCULATION_COMPLETED, CashFlowEvents.REVERSED_ETL_START_EVENT);

            put(CashFlowStates.REVERSED_ETL_START, CashFlowEvents.REVERSED_ETL_SENT_TO_DRP_EVENT);
            put(CashFlowStates.REVERSED_ETL_SENT_TO_DRP, CashFlowEvents.REVERSED_ETL_ACCEPTED_EVENT);
            put(CashFlowStates.REVERSED_ETL_ACCEPTED, CashFlowEvents.REVERSED_ETL_COMPLETED_EVENT);
            put(CashFlowStates.REVERSED_ETL_COMPLETED, CashFlowEvents.DATA_COPIED_FROM_STAGED_EVENT);

            put(CashFlowStates.DATA_COPIED_FROM_STAGED, CashFlowEvents.CALCULATION_FINISHED_EVENT);
        }
    };
    private static final Iterator<VssdvEvents> VSSDV_EVENTS = List.of(
            VssdvEvents.VAR_MODEL_DATA_PREPARED_EVENT,
            VssdvEvents.VAR_MODEL_DATA_COPIED_TO_STAGED_EVENT,
            VssdvEvents.VAR_MODEL_ETL_START_EVENT,
            VssdvEvents.VAR_MODEL_ETL_SENT_TO_DRP_EVENT,
            VssdvEvents.BLACK_MODEL_DATA_COPIED_TO_STAGED_EVENT,
            VssdvEvents.BLACK_MODEL_ETL_START_EVENT,
            VssdvEvents.VAR_MODEL_ETL_ACCEPTED_EVENT,
            VssdvEvents.BLACK_MODEL_ETL_SENT_TO_DRP_EVENT,
            VssdvEvents.VAR_MODEL_ETL_COMPLETED_EVENT
    ).iterator();

    private final Map<CalculationType, NextStateExecutor> executorMap;

    @Autowired
    public TestTransitionServiceImpl(
            CashFlowStateMachineService cashFlowStateMachineService,
            VssdvStateMachineService vssdvStateMachineService
    ) {
        this.executorMap = Map.of(
                CalculationType.CASH_FLOW, new CashFlowNextStateExecutor(cashFlowStateMachineService),
                CalculationType.VSSDV, new VssdvNextStateExecutor(vssdvStateMachineService)
        );
    }


    @Override
    public List<String> nextState(@NotNull CalculationType type, @NotNull UUID calculationUid) {
        return executorMap
                .get(type)
                .nextState(calculationUid);
    }

    private interface NextStateExecutor {
        List<String> nextState(@NotNull UUID calculationUid);

        CalculationType type();
    }

    private abstract static class BaseNextStateExecutor<States extends Enum<States>, Events extends Enum<Events>>
            implements NextStateExecutor {

        @Override
        public List<String> nextState(@NotNull UUID calculationUid) {
            final var stateMachine = stateMachineService()
                    .acquireStateMachine(calculationUid.toString());

            final var state = stateMachine.getState().getId();
            logger.info("Current SM '{}' for UID '{}' with state '{}'",
                        toHexString(stateMachine.hashCode()), stateMachine.getUuid(), state);

            final var event = event(state);
            final var message = withPayload(event).build();
            final var result = stateMachine.sendEvent(just(message)).blockLast();

            if (result.getResultType() != ResultType.ACCEPTED) {
                throw new IllegalStateException(
                        "State Machine '" + calculationUid + "' has wrong state '" +
                                result.getRegion().getState().getIds() + "' for transition " + event);
            }

            return stateMachine
                    .getState()
                    .getIds()
                    .stream()
                    .map(Enum::name)
                    .collect(toList());
        }

        protected abstract Events event(States state);

        protected abstract StateMachineService<States, Events> stateMachineService();
    }

    @RequiredArgsConstructor
    private static class CashFlowNextStateExecutor
            extends BaseNextStateExecutor<CashFlowStates, CashFlowEvents> {
        private final CashFlowStateMachineService cashFlowStateMachineService;

        @Override
        protected CashFlowEvents event(CashFlowStates state) {
            return CASH_FLOW_EVENTS.get(state);
        }

        @Override
        protected StateMachineService<CashFlowStates, CashFlowEvents> stateMachineService() {
            return cashFlowStateMachineService;
        }

        @Override
        public CalculationType type() {
            return CalculationType.CASH_FLOW;
        }
    }

    @RequiredArgsConstructor
    private static class VssdvNextStateExecutor
            extends BaseNextStateExecutor<VssdvStates, VssdvEvents> {
        private final VssdvStateMachineService vssdvStateMachineService;

        @Override
        protected VssdvEvents event(VssdvStates state) {
            return VSSDV_EVENTS.next();
        }

        @Override
        protected StateMachineService<VssdvStates, VssdvEvents> stateMachineService() {
            return vssdvStateMachineService;
        }

        @Override
        public CalculationType type() {
            return CalculationType.CASH_FLOW;
        }
    }
}
