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
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;

@Service
@RequiredArgsConstructor
public class CustomStateMachineService
        implements StateMachineService<States, Events>,
                   DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CustomStateMachineService.class);

    private final StateMachinePersist<States, Events, String> stateMachinePersist;
    private final StateMachineFactory<States, Events> stateMachineFactory;
    private final Map<String, StateMachine<States, Events>> machines = new ConcurrentHashMap<>();

    @Override
    public final void destroy() {
        for (var machineId : machines.keySet()) {
            releaseStateMachine(machineId, true);
        }
    }

    @Override
    public StateMachine<States, Events> acquireStateMachine(@NotNull String machineId) {
        return acquireStateMachine(machineId, true);
    }

    @Override
    @SneakyThrows
    public StateMachine<States, Events> acquireStateMachine(@NotNull String machineId, boolean start) {
        logger.info("Acquiring StateMachine with ID '{}'", machineId);

        var stateMachine = machines.get(machineId);
        if (Objects.isNull(stateMachine)) {
            stateMachine = stateMachineFactory.getStateMachine(machineId);
            machines.put(machineId, stateMachine);
        }

        var stateMachineContext = stateMachinePersist.read(machineId);
        stateMachine = restoreStateMachine(stateMachine, stateMachineContext);

        return handleStart(stateMachine, start);
    }

    @Override
    public void releaseStateMachine(@NotNull String machineId) {
        var stateMachine = machines.remove(machineId);
        if (stateMachine != null) {
            logger.info("Releasing StateMachine with id '{}'", machineId);
            stateMachine.stopReactively().block();
        }
    }

    @Override
    public void releaseStateMachine(@NotNull String machineId, boolean stop) {
        var stateMachine = machines.remove(machineId);
        if (stateMachine != null) {
            logger.info("Releasing StateMachine with id '{}'", machineId);
            handleStop(stateMachine, stop);
        }
    }

    @NotNull
    protected StateMachine<States, Events> restoreStateMachine(
            @NotNull StateMachine<States, Events> stateMachine,
            @Nullable StateMachineContext<States, Events> stateMachineContext) {
        if (stateMachineContext == null) {
            return stateMachine;
        }
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(
                            function -> function.resetStateMachineReactively(stateMachineContext)
                                                .block());
        return stateMachine;
    }

    @NotNull
    @SneakyThrows
    protected StateMachine<States, Events> handleStart(@NotNull StateMachine<States, Events> stateMachine,
                                                       boolean start) {
        if (start) {
            if (!((Lifecycle) stateMachine).isRunning()) {
                var listener = new StartListener(stateMachine);
                stateMachine.addStateListener(listener);
                stateMachine.startReactively().block();
                listener.latch.await();
            }
        }
        return stateMachine;
    }

    @SneakyThrows
    protected void handleStop(@NotNull StateMachine<States, Events> stateMachine, boolean stop) {
        if (stop) {
            if (((Lifecycle) stateMachine).isRunning()) {
                var listener = new StopListener(stateMachine);
                stateMachine.addStateListener(listener);
                stateMachine.stopReactively().block();
                listener.latch.await();
            }
        }
    }

    private static class StartListener
            extends StateMachineListenerAdapter<States, Events> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final StateMachine<States, Events> stateMachine;

        public StartListener(StateMachine<States, Events> stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public void stateMachineStarted(StateMachine<States, Events> stateMachine) {
            this.stateMachine.removeStateListener(this);
            latch.countDown();
        }
    }

    private static class StopListener
            extends StateMachineListenerAdapter<States, Events> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final StateMachine<States, Events> stateMachine;

        public StopListener(StateMachine<States, Events> stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public void stateMachineStopped(StateMachine<States, Events> stateMachine) {
            this.stateMachine.removeStateListener(this);
            latch.countDown();
        }
    }
}
