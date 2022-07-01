package ru.romanow.state.machine.models;

public enum States {
    DATA_PREPARED,             // Выполнены условия для старта расчета

    ETL_START,                 // Старт загрузки в DRP
    ETL_SEND_TO_DRP,           //   – Отправлен запрос в DRP
    ETL_ACCEPTED,              //   – DRP приступил к выполнению
    ETL_COMPLETED,             // Все файлы выгружены в DRP

    CALCULATION_START,         // Начало расчета в DRP
    CALCULATION_SENT_TO_DRP,   //   – Отправлен запрос в DRP
    CALCULATION_ACCEPTED,      //   – DRP приступил к выполнению
    CALCULATION_COMPLETED,     // Окончание расчета в DRP

    REVERSED_ETL_START,        // Начало загрузки результатов из DRP
    REVERSED_ETL_SENT_TO_DRP,  //   – Отправлен запрос в DRP
    REVERSED_ETL_ACCEPTED,     //   – DRP приступил к выполнению
    REVERSED_COMPLETED,        // Окончание загрузки результатов из DRP

    CALCULATION_FINISHED       // Расчет завершен
}
