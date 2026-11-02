import type { StatusTone } from "@/utils/amis/presets"

/**
 * Shared status label and tone maps.
 *
 * Each map pairs a backend enum value with a locale key and a semantic tone.
 * Pages pass a map straight to `statusColumn`:
 *
 *   statusColumn("status", "table.status", apiLogStatus)
 *
 * Append a map only when a page needs it. Labels must exist in both locale
 * files; `npm run lint:amis` checks them.
 */
export interface StatusMap {
    labels: Record<string, string>
    tones: Record<string, StatusTone>
}

export const apiLogStatus: StatusMap = {
    labels: {
        NEW: "status.apiLogNew",
        SUCCESS: "status.apiLogSuccess",
        FAIL: "status.apiLogFail"
    },
    tones: {
        NEW: "warning",
        SUCCESS: "success",
        FAIL: "danger"
    }
}

export const loginResultStatus: StatusMap = {
    labels: {
        "1": "status.loginSuccess",
        "0": "status.loginFail"
    },
    tones: {
        "1": "success",
        "0": "danger"
    }
}

export const acceptOrderStatus: StatusMap = {
    labels: {
        NEW: "status.acceptOrderNew",
        COMPLETE: "status.acceptOrderComplete"
    },
    tones: {
        NEW: "info",
        COMPLETE: "success"
    }
}

export const inboundPlanOrderStatus: StatusMap = {
    labels: {
        NEW: "status.inboundPlanNew",
        ACCEPTING: "status.inboundPlanAccepting",
        ACCEPTED: "status.inboundPlanAccepted",
        CANCEL: "status.inboundPlanCancel",
        CLOSED: "status.inboundPlanClosed"
    },
    tones: {
        NEW: "info",
        ACCEPTING: "warning",
        ACCEPTED: "success",
        CANCEL: "muted",
        CLOSED: "muted"
    }
}

export const putAwayTaskStatus: StatusMap = {
    labels: {
        NEW: "status.putAwayNew",
        PUTTING_AWAY: "status.putAwayInProgress",
        PUTTED_AWAY: "status.putAwayDone"
    },
    tones: {
        NEW: "info",
        PUTTING_AWAY: "warning",
        PUTTED_AWAY: "success"
    }
}

export const outboundPlanOrderStatus: StatusMap = {
    labels: {
        NEW: "status.outboundPlanNew",
        SHORT_WAITING: "status.outboundPlanShortWaiting",
        ASSIGNED: "status.outboundPlanAssigned",
        DISPATCHED: "status.outboundPlanDispatched",
        PICKING: "status.outboundPlanPicking",
        PICKED: "status.outboundPlanPicked",
        CANCELED: "status.outboundPlanCanceled"
    },
    tones: {
        NEW: "info",
        SHORT_WAITING: "warning",
        ASSIGNED: "info",
        DISPATCHED: "info",
        PICKING: "warning",
        PICKED: "success",
        CANCELED: "muted"
    }
}

export const pickingOrderStatus: StatusMap = {
    labels: {
        NEW: "status.pickingNew",
        DISPATCHED: "status.pickingDispatched",
        PICKING: "status.pickingInProgress",
        HANGING: "status.pickingHanging",
        PICKED: "status.pickingPicked",
        CANCELED: "status.pickingCanceled"
    },
    tones: {
        NEW: "info",
        DISPATCHED: "info",
        PICKING: "warning",
        HANGING: "danger",
        PICKED: "success",
        CANCELED: "muted"
    }
}

export const emptyContainerOutboundStatus: StatusMap = {
    labels: {
        NEW: "status.emptyContainerOutboundNew",
        PENDING: "status.emptyContainerOutboundPending",
        FINISHED: "status.emptyContainerOutboundFinished",
        ABNORMAL: "status.emptyContainerOutboundAbnormal",
        CANCELED: "status.emptyContainerOutboundCanceled"
    },
    tones: {
        NEW: "info",
        PENDING: "warning",
        FINISHED: "success",
        ABNORMAL: "danger",
        CANCELED: "muted"
    }
}

export const emptyContainerOutboundDetailStatus: StatusMap = {
    labels: {
        UNDO: "status.emptyContainerOutboundDetailUndo",
        DONE: "status.emptyContainerOutboundDetailDone",
        CANCELED: "status.emptyContainerOutboundDetailCanceled"
    },
    tones: {
        UNDO: "info",
        DONE: "success",
        CANCELED: "muted"
    }
}

export const stocktakeOrderStatus: StatusMap = {
    labels: {
        NEW: "status.stocktakeNew",
        STARTED: "status.stocktakeStarted",
        PARTIAL_DONE: "status.stocktakePartialDone",
        DONE: "status.stocktakeDone",
        CANCELED: "status.stocktakeCanceled"
    },
    tones: {
        NEW: "info",
        STARTED: "warning",
        PARTIAL_DONE: "warning",
        DONE: "success",
        CANCELED: "muted"
    }
}

export const stocktakeRecordStatus: StatusMap = {
    labels: {
        NEW: "status.stocktakeRecordNew",
        DONE: "status.stocktakeRecordDone",
        CLOSE: "status.stocktakeRecordClosed"
    },
    tones: {
        NEW: "info",
        DONE: "success",
        CLOSE: "muted"
    }
}

export const stockAdjustmentOrderStatus: StatusMap = {
    labels: {
        NEW: "status.stockAdjustNew",
        COMPLETE: "status.stockAdjustComplete",
        CLOSED: "status.stockAdjustClosed"
    },
    tones: {
        NEW: "info",
        COMPLETE: "success",
        CLOSED: "muted"
    }
}

export const stockAbnormalStatus: StatusMap = {
    labels: {
        NEW: "status.stockAbnormalNew",
        CREATE_RE_CHECK_ORDER: "status.stockAbnormalRecheckCreated",
        CREATE_ADJUSTMENT_ORDER: "status.stockAbnormalAdjustmentCreated",
        FINISH_RE_CHECK_ORDER: "status.stockAbnormalRecheckFinished",
        FINISH_ADJUSTMENT_ORDER: "status.stockAbnormalAdjustmentFinished",
        CLOSED: "status.stockAbnormalClosed"
    },
    tones: {
        NEW: "info",
        CREATE_RE_CHECK_ORDER: "warning",
        CREATE_ADJUSTMENT_ORDER: "warning",
        FINISH_RE_CHECK_ORDER: "success",
        FINISH_ADJUSTMENT_ORDER: "success",
        CLOSED: "muted"
    }
}

export const transferContainerStatus: StatusMap = {
    labels: {
        IDLE: "status.transferContainerIdle",
        OCCUPANCY: "status.transferContainerOccupancy",
        LOCKED: "status.transferContainerLocked"
    },
    tones: {
        IDLE: "muted",
        OCCUPANCY: "warning",
        LOCKED: "danger"
    }
}

export const printStatus: StatusMap = {
    labels: {
        PRINT_SUCCESS: "status.printSuccess",
        PRINT_FAIL: "status.printFail",
        NOT_PRINTED: "status.printNotPrinted"
    },
    tones: {
        PRINT_SUCCESS: "success",
        PRINT_FAIL: "danger",
        NOT_PRINTED: "muted"
    }
}

export const locationStatus: StatusMap = {
    labels: {
        PUT_AWAY_ONLY: "status.locationPutAwayOnly",
        TAKE_OFF_ONLY: "status.locationTakeOffOnly",
        PUT_AWAY_PUT_DOWN: "status.locationPutAwayPutDown",
        NONE: "status.locationDisabled"
    },
    tones: {
        PUT_AWAY_ONLY: "info",
        TAKE_OFF_ONLY: "warning",
        PUT_AWAY_PUT_DOWN: "success",
        NONE: "muted"
    }
}
