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
