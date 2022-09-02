package ru.romanow.state.machine.service.vssdv;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.StateDescriptor.StateMachineType;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;
import ru.romanow.state.machine.service.CalculationStatusService;

import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.BLACK_MODEL;
import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.VAR_MODEL;

@Service
public class VssdvCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<VssdvStates, VssdvEvents> {

    @Autowired
    public VssdvCustomStateMachinePersist(CalculationStatusService calculationStatusService) {
        super(calculationStatusService);
    }

    @Override
    protected VssdvStates restoreState(@NotNull String state) {
        return VssdvStates.valueOf(state);
    }

    @Override
    protected List<StateMachineType> secondaryMachineTypes() {
        return List.of(VAR_MODEL, BLACK_MODEL);
    }

}
