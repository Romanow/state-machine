package ru.romanow.state.machine.repostitory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.romanow.state.machine.domain.VssdvCalculationStatus;
import ru.romanow.state.machine.models.vssdv.VssdvStates;

public interface VssdvCalculationStatusRepository
        extends CalculationStatusRepository<VssdvStates, VssdvCalculationStatus> {

    @Override
    @Query("select cs.status "
            + "from VssdvCalculationStatus cs "
            + "where cs.calculation.uid = :calculationUid "
            + "order by cs.createdDate desc ")
    List<VssdvStates> getCalculationLastState(@Param("calculationUid") UUID calculationUid, Pageable pageable);
}
