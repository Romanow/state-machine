package ru.romanow.state.machine.service.vssdv;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;
import ru.romanow.state.machine.service.CalculationService;
import ru.romanow.state.machine.service.CalculationStatusService;

@Service
public class VssdvCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<VssdvStates, VssdvEvents> {

    @Autowired
    public VssdvCustomStateMachinePersist(CalculationService calculationService,
                                          CalculationStatusService calculationStatusService) {
        super(calculationService, calculationStatusService);
    }

    @Override
    protected VssdvStates restoreState(@NotNull String state) {
        return VssdvStates.valueOf(state);
    }
}
