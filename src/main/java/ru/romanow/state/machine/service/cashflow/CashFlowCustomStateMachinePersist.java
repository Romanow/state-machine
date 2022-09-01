package ru.romanow.state.machine.service.cashflow;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.models.cashflow.CashFlowEvents;
import ru.romanow.state.machine.models.cashflow.CashFlowStates;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;
import ru.romanow.state.machine.service.CalculationStatusService;

@Service
public class CashFlowCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<CashFlowStates, CashFlowEvents> {

    @Autowired
    public CashFlowCustomStateMachinePersist(CalculationStatusService calculationStatusService) {
        super(calculationStatusService);
    }

    @Override
    protected CashFlowStates restoreState(@NotNull String state) {
        return CashFlowStates.valueOf(state);
    }
}
