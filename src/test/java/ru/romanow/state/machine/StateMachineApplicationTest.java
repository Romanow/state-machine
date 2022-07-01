package ru.romanow.state.machine;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.romanow.state.machine.config.DatabaseTestConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.repostitory.CalculationRepository;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static reactor.core.publisher.Mono.just;

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration.class)
class StateMachineApplicationTest {

    public static final UUID CALCULATION_UID_1 = UUID.randomUUID();
    public static final UUID CALCULATION_UID_2 = UUID.randomUUID();

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @BeforeEach
    void init() {
        calculationRepository.saveAll(
                of(buildCalculation(CALCULATION_UID_1),
                   buildCalculation(CALCULATION_UID_2))
        );
    }

    @Test
    void test() {
        var stateMachine = stateMachineFactory.getStateMachine(CALCULATION_UID_1);

        assertThat(stateMachine.getState().getId()).isEqualTo(States.DATA_PREPARED);

        stateMachine.sendEvent(just(withPayload(Events.ETL_START_EVENT).build())).subscribe();

        assertThat(stateMachine.getState().getId()).isEqualTo(States.ETL_START);
    }

    private Calculation buildCalculation(@NotNull UUID uid) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(CalculationType.CASH_FLOW)
                .setDescription(randomAlphabetic(20));
    }

}