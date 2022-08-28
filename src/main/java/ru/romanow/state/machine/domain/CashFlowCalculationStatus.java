package ru.romanow.state.machine.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.romanow.state.machine.models.cashflow.CashflowStates;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@DiscriminatorValue(CalculationTypes.CASHFLOW)
public class CashFlowCalculationStatus
        extends CalculationStatus<CashflowStates> {

    public CashFlowCalculationStatus() {
    }

    public CashFlowCalculationStatus(CashflowStates status) {
        this.status = status;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CashflowStates status;

}
