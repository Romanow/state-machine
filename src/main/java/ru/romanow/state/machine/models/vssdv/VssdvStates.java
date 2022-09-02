package ru.romanow.state.machine.models.vssdv;

import org.jetbrains.annotations.NotNull;
import ru.romanow.state.machine.models.StateDescriptor;

import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.BLACK_MODEL;
import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.MAIN;
import static ru.romanow.state.machine.models.StateDescriptor.StateMachineType.VAR_MODEL;

public enum VssdvStates
        implements StateDescriptor {
    CALCULATION_STARTED,

    // region Var Model
    // #################################
    // ########### VaR Model ###########
    // #################################
    VAR_MODEL_CALCULATION_STARTED(VAR_MODEL),
    VAR_MODEL_DATA_PREPARED(VAR_MODEL),
    VAR_MODEL_DATA_COPIED_TO_STAGED(VAR_MODEL),

    // ETL
    VAR_MODEL_ETL_START(VAR_MODEL),
    VAR_MODEL_ETL_SENT_TO_DRP(VAR_MODEL),
    VAR_MODEL_ETL_ACCEPTED(VAR_MODEL),
    VAR_MODEL_ETL_COMPLETED(VAR_MODEL),

    // Calculation
    VAR_MODEL_CALCULATION_START(VAR_MODEL),
    VAR_MODEL_CALCULATION_SENT_TO_DRP(VAR_MODEL),
    VAR_MODEL_CALCULATION_ACCEPTED(VAR_MODEL),
    VAR_MODEL_CALCULATION_COMPLETED(VAR_MODEL),

    // Reverse ETL
    VAR_MODEL_REVERSED_ETL_START(VAR_MODEL),
    VAR_MODEL_REVERSED_ETL_SENT_TO_DRP(VAR_MODEL),
    VAR_MODEL_REVERSED_ETL_ACCEPTED(VAR_MODEL),
    VAR_MODEL_REVERSED_ETL_COMPLETED(VAR_MODEL),

    VAR_MODEL_DATA_COPIED_FROM_STAGED(VAR_MODEL),
    VAR_MODEL_CALCULATION_FINISHED(VAR_MODEL),
    // endregion

    // region Black Model
    // #################################
    // ########## Black Model ##########
    // #################################
    BLACK_MODEL_CALCULATION_STARTED(BLACK_MODEL),
    BLACK_MODEL_DATA_PREPARED(BLACK_MODEL),
    BLACK_MODEL_DATA_COPIED_TO_STAGED(BLACK_MODEL),

    // ETL
    BLACK_MODEL_ETL_START(BLACK_MODEL),
    BLACK_MODEL_ETL_SENT_TO_DRP(BLACK_MODEL),
    BLACK_MODEL_ETL_ACCEPTED(BLACK_MODEL),
    BLACK_MODEL_ETL_COMPLETED(BLACK_MODEL),

    // Calculation
    BLACK_MODEL_CALCULATION_START(BLACK_MODEL),
    BLACK_MODEL_CALCULATION_SENT_TO_DRP(BLACK_MODEL),
    BLACK_MODEL_CALCULATION_ACCEPTED(BLACK_MODEL),
    BLACK_MODEL_CALCULATION_COMPLETED(BLACK_MODEL),

    // Reverse ETL
    BLACK_MODEL_REVERSED_ETL_START(BLACK_MODEL),
    BLACK_MODEL_REVERSED_ETL_SENT_TO_DRP(BLACK_MODEL),
    BLACK_MODEL_REVERSED_ETL_ACCEPTED(BLACK_MODEL),
    BLACK_MODEL_REVERSED_ETL_COMPLETED(BLACK_MODEL),

    BLACK_MODEL_DATA_COPIED_FROM_STAGED(BLACK_MODEL),
    BLACK_MODEL_CALCULATION_FINISHED(BLACK_MODEL),
    // endregion

    // region VSSDV
    // #################################
    // ############ VSSDV ##############
    // #################################
    VSSDV_JOIN_STATE,
    VSSDV_CALCULATION_STARTED,
    VSSDV_DATA_PREPARED,
    VSSDV_DATA_COPIED_TO_STAGED,

    // ETL
    VSSDV_ETL_START,
    VSSDV_ETL_SENT_TO_DRP,
    VSSDV_ETL_ACCEPTED,
    VSSDV_ETL_COMPLETED,

    // Calculation
    VSSDV_CALCULATION_START,
    VSSDV_CALCULATION_SENT_TO_DRP,
    VSSDV_CALCULATION_ACCEPTED,
    VSSDV_CALCULATION_COMPLETED,

    // Reverse ETL
    VSSDV_REVERSED_ETL_START,
    VSSDV_REVERSED_ETL_SENT_TO_DRP,
    VSSDV_REVERSED_ETL_ACCEPTED,
    VSSDV_REVERSED_ETL_COMPLETED,

    VSSDV_DATA_COPIED_FROM_STAGED,
    VSSDV_CALCULATION_FINISHED,
    // endregion

    CALCULATION_ERROR;

    private final StateMachineType descriptor;

    VssdvStates(@NotNull StateMachineType descriptor) {
        this.descriptor = descriptor;
    }

    VssdvStates() {
        this(MAIN);
    }

    @Override
    public StateMachineType type() {
        return descriptor;
    }
}
