package ru.romanow.state.machine.service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.CashflowEvents;
import ru.romanow.state.machine.models.CashflowStates;

@Service
@RequiredArgsConstructor
public class StateMachineServiceImpl
        implements StateMachineService,
                   DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineServiceImpl.class);

    private final StateMachinePersist<CashflowStates, CashflowEvents, String> stateMachinePersist;
    private final Map<String, StateMachineFactory<CashflowStates, CashflowEvents>> stateMachineFactories;
    private final Map<String, StateMachine<CashflowStates, CashflowEvents>> machines = new ConcurrentHashMap<>();

    @Override
    public final void destroy() {
        for (var machineId : machines.keySet()) {
            releaseStateMachine(machineId);
        }
    }

    @NotNull
    @Override
    @SneakyThrows
    public StateMachine<CashflowStates, CashflowEvents> acquireStateMachine(@NotNull String type, @NotNull String machineId) {
        logger.info("Acquiring StateMachine with ID '{}'", machineId);

        var stateMachine = machines.get(machineId);
        if (Objects.isNull(stateMachine)) {
            stateMachine = stateMachineFactories
                    .get("cashflow")
                    .getStateMachine(machineId);
            machines.put(machineId, stateMachine);
        }

        var stateMachineContext = stateMachinePersist.read(machineId);
        stateMachine = restoreStateMachine(stateMachine, stateMachineContext);

        return handleStart(stateMachine);
    }

    @Override
    public void releaseStateMachine(@NotNull String machineId) {
        var stateMachine = machines.remove(machineId);
        if (stateMachine != null) {
            logger.info("Releasing StateMachine with id '{}'", machineId);
            handleStop(stateMachine);
        }
    }

    @NotNull
    protected StateMachine<CashflowStates, CashflowEvents> restoreStateMachine(
            @NotNull StateMachine<CashflowStates, CashflowEvents> stateMachine,
            @Nullable StateMachineContext<CashflowStates, CashflowEvents> stateMachineContext) {
        if (stateMachineContext == null) {
            return stateMachine;
        }
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(f -> f.resetStateMachineReactively(stateMachineContext).block());
        return stateMachine;
    }

    @NotNull
    @SneakyThrows
    protected StateMachine<CashflowStates, CashflowEvents> handleStart(@NotNull StateMachine<CashflowStates, CashflowEvents> stateMachine) {
        if (!((Lifecycle) stateMachine).isRunning()) {
            var listener = new StartListener(stateMachine);
            stateMachine.addStateListener(listener);
            stateMachine.startReactively().block();
            listener.latch.await();
        }
        return stateMachine;
    }

    @SneakyThrows
    protected void handleStop(@NotNull StateMachine<CashflowStates, CashflowEvents> stateMachine) {
        if (((Lifecycle) stateMachine).isRunning()) {
            var listener = new StopListener(stateMachine);
            stateMachine.addStateListener(listener);
            stateMachine.stopReactively().block();
            listener.latch.await();
        }
    }

    private static class StartListener
            extends StateMachineListenerAdapter<CashflowStates, CashflowEvents> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final StateMachine<CashflowStates, CashflowEvents> stateMachine;

        public StartListener(StateMachine<CashflowStates, CashflowEvents> stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public void stateMachineStarted(StateMachine<CashflowStates, CashflowEvents> stateMachine) {
            this.stateMachine.removeStateListener(this);
            latch.countDown();
        }
    }

    private static class StopListener
            extends StateMachineListenerAdapter<CashflowStates, CashflowEvents> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final StateMachine<CashflowStates, CashflowEvents> stateMachine;

        public StopListener(StateMachine<CashflowStates, CashflowEvents> stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public void stateMachineStopped(StateMachine<CashflowStates, CashflowEvents> stateMachine) {
            this.stateMachine.removeStateListener(this);
            latch.countDown();
        }
    }
}
