package ru.romanow.state.machine;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.state.machine.config.DatabaseTestConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.models.Events;
import ru.romanow.state.machine.models.States;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.service.CustomStateMachineService;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static reactor.core.publisher.Mono.just;

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration.class)
@Transactional
@AutoConfigureTestEntityManager
class StateMachineApplicationTest {

    public static final UUID CALCULATION_UID_1 = UUID.fromString("639cb402-3ae4-4ff4-ab1a-d70eaa661334");
    public static final UUID CALCULATION_UID_2 = UUID.fromString("07dabafa-529d-4da4-bab5-a6359313c064");

    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CustomStateMachineService stateMachineService;

    @BeforeEach
    void init() {
        calculationRepository.saveAll(
                of(buildCalculation(CALCULATION_UID_1), buildCalculation(CALCULATION_UID_2))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "639cb402-3ae4-4ff4-ab1a-d70eaa661334",
            "07dabafa-529d-4da4-bab5-a6359313c064"
    })
    void test(String uid) {
        var stateMachine = stateMachineService.acquireStateMachine(uid);

        assertThat(stateMachine.getState().getId()).isEqualTo(States.CALCULATION_STARTED);
        stateMachine.sendEvent(just(withPayload(Events.DATA_PREPARED_EVENT).build())).subscribe();
        assertThat(stateMachine.getState().getId()).isEqualTo(States.DATA_PREPARED);

        stateMachine = stateMachineService.acquireStateMachine(uid);

        stateMachine.sendEvent(just(withPayload(Events.ETL_START_EVENT).build())).subscribe();
        assertThat(stateMachine.getState().getId()).isEqualTo(States.ETL_START);
    }

    @NotNull
    private Calculation buildCalculation(@NotNull UUID uid) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(CalculationType.CASH_FLOW)
                .setDescription(randomAlphabetic(20));
    }

}