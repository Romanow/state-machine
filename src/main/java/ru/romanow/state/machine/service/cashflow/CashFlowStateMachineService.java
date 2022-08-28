package ru.romanow.state.machine.service.cashflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.CashFlowCalculationStatus;
import ru.romanow.state.machine.models.cashflow.CashflowEvents;
import ru.romanow.state.machine.models.cashflow.CashflowStates;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;
import ru.romanow.state.machine.service.BaseStateMachineService;

@Service
public class CashFlowStateMachineService
        extends BaseStateMachineService<CashflowStates, CashflowEvents, CashFlowCalculationStatus, CashFlowCalculationStatusRepository> {

    @Autowired
    public CashFlowStateMachineService(
            BaseCustomStateMachinePersist<CashflowStates, CashflowEvents, CashFlowCalculationStatus, CashFlowCalculationStatusRepository> stateMachinePersist,
            StateMachineFactory<CashflowStates, CashflowEvents> stateMachineFactory) {
        super(stateMachinePersist, stateMachineFactory);
    }
}
