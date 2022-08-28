package ru.romanow.state.machine.service.vssdv;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultStateMachineContext;
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

    @Override
    public StateMachineContext<VssdvStates, VssdvEvents> read(String machineId) {
        var children = List.<StateMachineContext<VssdvStates, VssdvEvents>>of(
                new DefaultStateMachineContext<>(VssdvStates.BLACK_MODEL_ETL_SEND_TO_DRP, null, null, null),
                new DefaultStateMachineContext<>(VssdvStates.VAR_MODEL_REVERSED_COMPLETED, null, null, null)
        );
        return new DefaultStateMachineContext<>(children, VssdvStates.CALCULATION_STARTED, null, null, null, null, machineId);
    }
}
