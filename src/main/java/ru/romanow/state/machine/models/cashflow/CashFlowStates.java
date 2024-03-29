package ru.romanow.state.machine.models.cashflow;

import org.springframework.statemachine.support.StateMachineUtils;
import ru.romanow.state.machine.models.StateDescriptor;

public enum CashFlowStates
        implements StateDescriptor {
    CALCULATION_STARTED,       // Начальное состояние

    DATA_PREPARED,             // Выполнены условия для старта расчета

    DATA_COPIED_TO_STAGED,     // Данные скопированы в схему staged

    ETL_START,                 // Старт загрузки в DRP
    ETL_SENT_TO_DRP,           //   – Отправлен запрос в DRP
    ETL_ACCEPTED,              //   – DRP приступил к выполнению
    ETL_COMPLETED,             // Все файлы выгружены в DRP

    CALCULATION_START,         // Начало расчета в DRP
    CALCULATION_SENT_TO_DRP,   //   – Отправлен запрос в DRP
    CALCULATION_ACCEPTED,      //   – DRP приступил к выполнению
    CALCULATION_COMPLETED,     // Окончание расчета в DRP

    REVERSED_ETL_START,        // Начало загрузки результатов из DRP
    REVERSED_ETL_SENT_TO_DRP,  //   – Отправлен запрос в DRP
    REVERSED_ETL_ACCEPTED,     //   – DRP приступил к выполнению
    REVERSED_ETL_COMPLETED,    // Окончание загрузки результатов из DRP

    DATA_COPIED_FROM_STAGED,   // Данные скопированы из staged схемы

    CALCULATION_FINISHED,      // Расчет завершен

    CALCULATION_ERROR;         // Расчет завершился с ошибкой

    final StateMachineType descriptor;
    CashFlowStates() {
        this.descriptor = StateMachineType.MAIN;
    }

    @Override
    public StateMachineType type() {
        return descriptor;
    }
}
