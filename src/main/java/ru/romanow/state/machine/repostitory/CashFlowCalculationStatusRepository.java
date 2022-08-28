package ru.romanow.state.machine.repostitory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.romanow.state.machine.domain.CashFlowCalculationStatus;
import ru.romanow.state.machine.models.cashflow.CashflowStates;

public interface CashFlowCalculationStatusRepository
        extends CalculationStatusRepository<CashflowStates, CashFlowCalculationStatus> {

    @Override
    @Query("select cs.status "
            + "from CashFlowCalculationStatus cs "
            + "where cs.calculation.uid = :calculationUid "
            + "order by cs.createdDate desc ")
    List<CashflowStates> getCalculationLastState(@Param("calculationUid") UUID calculationUid, Pageable pageable);
}
