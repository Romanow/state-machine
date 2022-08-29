package ru.romanow.state.machine.service.vssdv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.vssdv.VssdvEvents;
import ru.romanow.state.machine.models.vssdv.VssdvStates;
import ru.romanow.state.machine.service.BaseStateMachineService;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;

@Service
public class VssdvStateMachineService
        extends BaseStateMachineService<VssdvStates, VssdvEvents> {

    @Autowired
    public VssdvStateMachineService(
            BaseCustomStateMachinePersist<VssdvStates, VssdvEvents> vssdvCustomStateMachinePersist,
            StateMachineFactory<VssdvStates, VssdvEvents> stateMachineFactory
    ) {
        super(vssdvCustomStateMachinePersist, stateMachineFactory);
    }
}
