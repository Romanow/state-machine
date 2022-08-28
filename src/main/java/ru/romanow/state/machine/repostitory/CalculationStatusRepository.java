package ru.romanow.state.machine.repostitory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import ru.romanow.state.machine.domain.CalculationStatus;

@NoRepositoryBean
public interface CalculationStatusRepository<States, CS extends CalculationStatus<States>>
        extends JpaRepository<CS, Long> {

    List<States> getCalculationLastState(@Param("calculationUid") UUID calculationUid, Pageable pageable);
}
