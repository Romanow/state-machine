package ru.romanow.state.machine.service.vssdv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.VssdvCalculationStatus;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.repostitory.VssdvCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseStateMachineService;

@Service
public class VssdvStateMachineService
        extends BaseStateMachineService<VssdvStates, VssdvEvents, VssdvCalculationStatus, VssdvCalculationStatusRepository> {

    @Autowired
    public VssdvStateMachineService(VssdvCustomStateMachinePersist vssdvCustomStateMachinePersist,
                                    StateMachineFactory<VssdvStates, VssdvEvents> stateMachineFactory) {
        super(vssdvCustomStateMachinePersist, stateMachineFactory);
    }
}
