package ru.romanow.state.machine.service.cashflow;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.romanow.state.machine.domain.CashFlowCalculationStatus;
import ru.romanow.state.machine.models.cashflow.CashflowEvents;
import ru.romanow.state.machine.models.cashflow.CashflowStates;
import ru.romanow.state.machine.repostitory.CalculationRepository;
import ru.romanow.state.machine.repostitory.CashFlowCalculationStatusRepository;
import ru.romanow.state.machine.service.BaseCustomStateMachinePersist;

@Service
public class CashFlowCustomStateMachinePersist
        extends BaseCustomStateMachinePersist<CashflowStates, CashflowEvents, CashFlowCalculationStatus, CashFlowCalculationStatusRepository> {

    @Autowired
    public CashFlowCustomStateMachinePersist(CalculationRepository calculationRepository,
                                             CashFlowCalculationStatusRepository cashFlowCalculationStatusRepository) {
        super(calculationRepository, cashFlowCalculationStatusRepository);
    }

    @Override
    protected CashFlowCalculationStatus buildCalculationStatus(@NotNull CashflowStates state) {
        return new CashFlowCalculationStatus(state);
    }
}
