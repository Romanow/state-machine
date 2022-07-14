package ru.romanow.state.machine.models;

public enum States {
    CASH_FLOW_CALCULATION_STARTED,       // Начальное состояние

    CASH_FLOW_DATA_PREPARED,             // Выполнены условия для старта расчета

    CASH_FLOW_ETL_START,                 // Старт загрузки в DRP
    CASH_FLOW_ETL_SEND_TO_DRP,           //   – Отправлен запрос в DRP
    CASH_FLOW_ETL_ACCEPTED,              //   – DRP приступил к выполнению
    CASH_FLOW_ETL_COMPLETED,             // Все файлы выгружены в DRP

    CASH_FLOW_CALCULATION_START,         // Начало расчета в DRP
    CASH_FLOW_CALCULATION_SENT_TO_DRP,   //   – Отправлен запрос в DRP
    CASH_FLOW_CALCULATION_ACCEPTED,      //   – DRP приступил к выполнению
    CASH_FLOW_CALCULATION_COMPLETED,     // Окончание расчета в DRP

    CASH_FLOW_REVERSED_ETL_START,        // Начало загрузки результатов из DRP
    CASH_FLOW_REVERSED_ETL_SENT_TO_DRP,  //   – Отправлен запрос в DRP
    CASH_FLOW_REVERSED_ETL_ACCEPTED,     //   – DRP приступил к выполнению
    CASH_FLOW_REVERSED_COMPLETED,        // Окончание загрузки результатов из DRP

    CASH_FLOW_CALCULATION_FINISHED       // Расчет завершен
}
