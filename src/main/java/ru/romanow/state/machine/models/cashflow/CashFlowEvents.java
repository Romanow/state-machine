package ru.romanow.state.machine.models.cashflow;

public enum CashFlowEvents {
    DATA_PREPARED_EVENT,              // CALCULATION_STARTED (init) ->   DATA_PREPARED_EVENT

    DATA_COPIED_TO_STAGED_EVENT,      // DATA_PREPARED_EVENT        ->   DATA_COPIED_TO_STAGED

    ETL_START_EVENT,                  // DATA_COPIED_TO_STAGED      ->   ETL_START
    ETL_SENT_TO_DRP_EVENT,            // ETL_START                  ->   ETL_SENT_TO_DRP
    ETL_ACCEPTED_EVENT,               // ETL_SENT_TO_DRP            ->   ETL_ACCEPTED
    ETL_COMPLETED_EVENT,              // ETL_ACCEPTED               ->   ETL_COMPLETED

    CALCULATION_START_EVENT,          // ETL_COMPLETED              ->   CALCULATION_START
    CALCULATION_SENT_TO_DRP_EVENT,    // CALCULATION_START          ->   CALCULATION_SENT_TO_DRP
    CALCULATION_ACCEPTED_EVENT,       // CALCULATION_SENT_TO_DRP    ->   CALCULATION_ACCEPTED
    CALCULATION_COMPLETED_EVENT,      // CALCULATION_ACCEPTED       ->   CALCULATION_COMPLETED

    REVERSED_ETL_START_EVENT,         // CALCULATION_COMPLETED      ->   REVERSED_ETL_START
    REVERSED_ETL_SENT_TO_DRP_EVENT,   // REVERSED_ETL_START         ->   REVERSED_ETL_SENT_TO_DRP
    REVERSED_ETL_ACCEPTED_EVENT,      // REVERSED_ETL_SENT_TO_DRP   ->   REVERSED_ETL_ACCEPTED
    REVERSED_ETL_COMPLETED_EVENT,     // REVERSED_ETL_ACCEPTED      ->   REVERSED_ETL_COMPLETED

    DATA_COPIED_FROM_STAGED_EVENT,    // REVERSED_ETL_COMPLETED     ->   DATA_COPIED_FROM_STAGED

    CALCULATION_FINISHED_EVENT,       // DATA_COPIED_FROM_STAGED    ->   CALCULATION_FINISHED (end)
    CALCULATION_ERROR_EVENT           // Every State                ->   CALCULATION_ERROR (end)
}
