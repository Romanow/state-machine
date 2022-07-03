package ru.romanow.state.machine.models;

public enum Events {
    DATA_PREPARED_EVENT,                            // CALCULATION_STARTED (init) ->   DATA_PREPARED_EVENT
    
    CALCULATION_START_WITHOUT_ETL_EVENT,            // DATA_PREPARED              ->   CALCULATION_START
    ETL_START_EVENT,                                // DATA_PREPARED              ->   ETL_START
    ETL_SENT_TO_DRP_EVENT,                          // ETL_START                  ->   ETL_SENT_TO_DRP
    ETL_ACCEPTED_EVENT,                             // ETL_SENT_TO_DRP            ->   ETL_ACCEPTED
    ETL_COMPLETED_EVENT,                            // ETL_ACCEPTED               ->   ETL_COMPLETED

    CALCULATION_START_EVENT,                        // ETL_COMPLETED              ->   CALCULATION_START
    CALCULATION_SENT_TO_DRP_EVENT,                  // CALCULATION_START          ->   CALCULATION_SENT_TO_DRP
    CALCULATION_ACCEPTED_EVENT,                     // CALCULATION_SENT_TO_DRP    ->   CALCULATION_ACCEPTED
    CALCULATION_COMPLETED_EVENT,                    // CALCULATION_ACCEPTED       ->   CALCULATION_COMPLETED

    CALCULATION_FINISHED_WITHOUT_REVERSE_ETL_EVENT, // CALCULATION_COMPLETED      ->   FINISH EVENT (end)
    REVERSED_ETL_START_EVENT,                       // CALCULATION_COMPLETED      ->   REVERSED_ETL_START
    REVERSED_ETL_SENT_TO_DRP_EVENT,                 // REVERSED_ETL_START         ->   REVERSED_ETL_SENT_TO_DRP
    REVERSED_ETL_ACCEPTED_EVENT,                    // REVERSED_ETL_SENT_TO_DRP   ->   REVERSED_ETL_ACCEPTED
    REVERSED_COMPLETED_EVENT,                       // REVERSED_ETL_ACCEPTED      ->   REVERSED_COMPLETED

    CALCULATION_FINISHED_EVENT                      // REVERSED_COMPLETED         ->   FINISH EVENT (end)
}
