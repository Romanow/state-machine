package ru.romanow.state.machine.statuses;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.state.machine.config.StateMachineConfiguration;
import ru.romanow.state.machine.domain.Calculation;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CalculationStatusRepository;
import ru.romanow.state.machine.statuses.StateMachineStatusTest.StateMachineStatusTestConfiguration;

import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

@ActiveProfiles("test")
@SpringBootTest(classes = StateMachineStatusTestConfiguration.class)
class StateMachineStatusTest {

    @Autowired
    protected CalculationRepository calculationRepository;

    @NotNull
    protected Calculation buildCalculation(@NotNull UUID uid, @NotNull CalculationType calculationType) {
        return new Calculation()
                .setUid(uid)
                .setName(randomAlphabetic(8))
                .setType(calculationType)
                .setDescription(randomAlphabetic(20));
    }

    @TestConfiguration
    @Import(StateMachineConfiguration.class)
    @MockBean(CalculationRepository.class)
    @MockBean(CalculationStatusRepository.class)
    static class StateMachineStatusTestConfiguration {
    }

}
