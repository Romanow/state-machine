package ru.romanow.state.machine.service.cashflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.CashFlowCalculationStatus;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseStateMachineService;

@Service
public class CashFlowStateMachineService
        extends BaseStateMachineService<CashFlowStates, CashFlowEvents, CashFlowCalculationStatus, CashFlowCalculationStatusRepository> {

    @Autowired
    public CashFlowStateMachineService(CashFlowCustomStateMachinePersist cashFlowCustomStateMachinePersist,
                                       StateMachineFactory<CashFlowStates, CashFlowEvents> stateMachineFactory) {
        super(cashFlowCustomStateMachinePersist, stateMachineFactory);
    }
}
