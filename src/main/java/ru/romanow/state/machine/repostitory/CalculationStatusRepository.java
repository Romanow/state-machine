package ru.romanow.state.machine.repostitory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.romanow.state.machine.domain.CalculationStatus;

public interface CalculationStatusRepository
        extends JpaRepository<CalculationStatus, Long> {

    @Query("select cs.status "
            + "from CalculationStatus cs "
            + "where cs.calculation.uid = :calculationUid "
            + "order by cs.createdDate desc ")
    List<String> getCalculationLastState(@Param("calculationUid") UUID calculationUid, Pageable pageable);
}
