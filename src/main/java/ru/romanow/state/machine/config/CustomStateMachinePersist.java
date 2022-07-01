package ru.romanow.state.machine.config;

import org.slf4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;

import static org.slf4j.LoggerFactory.getLogger;

@ComponentScan
public class CustomStateMachinePersist
        implements StateMachinePersist<States, Events, String> {

    private static final Logger logger = getLogger(CustomStateMachinePersist.class);

    @Override
    public void write(StateMachineContext<States, Events> context, String contextObj) {
        logger.info("{}", context);
    }

    @Override
    public StateMachineContext<States, Events> read(String contextObj) {
        logger.info("{}", contextObj);
        return null;
    }
}
