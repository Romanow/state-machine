package ru.romanow.state.machine.service.vssdv;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.VssdvCalculationStatus;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.VssdvCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;

@Service
public class VssdvCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<VssdvStates, VssdvEvents, VssdvCalculationStatus, VssdvCalculationStatusRepository> {

    @Autowired
    public VssdvCustomStateMachinePersist(CalculationRepository calculationRepository,
                                          VssdvCalculationStatusRepository calculationStatusRepository) {
        super(calculationRepository, calculationStatusRepository);
    }

    @Override
    protected VssdvCalculationStatus buildCalculationStatus(@NotNull VssdvStates state) {
        return new VssdvCalculationStatus(state);
    }
}
