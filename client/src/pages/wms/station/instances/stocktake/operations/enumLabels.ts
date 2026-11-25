import type {TFunction} from "i18next"

const METHOD_KEYS: Record<string, string> = {
    INFORMED: "stocktake.station.method.informed",
    BLIND: "stocktake.station.method.blind",
    // Older backend spelling kept for cached station views.
    BLIND_QTY: "stocktake.station.method.blind"
}

const TYPE_KEYS: Record<string, string> = {
    ORDINARY: "stocktake.station.type.ordinary",
    RANDOM: "stocktake.station.type.random",
    ACTIVE: "stocktake.station.type.active",
    DISCREPANCY_REVIEW: "stocktake.station.type.discrepancyReview"
}

const labelFor = (
    raw: string | undefined,
    keys: Record<string, string>,
    t: TFunction
): string => {
    if (!raw) return "—"
    const normalized = String(raw).toUpperCase()
    return keys[normalized] ? t(keys[normalized]) : raw
}

export const stocktakeMethodLabel = (
    method: string | undefined,
    t: TFunction
): string => labelFor(method, METHOD_KEYS, t)

export const stocktakeTypeLabel = (
    type: string | undefined,
    t: TFunction
): string => labelFor(type, TYPE_KEYS, t)

export const isBlindStocktake = (method?: string): boolean => {
    const normalized = String(method || "").toUpperCase()
    return normalized === "BLIND" || normalized === "BLIND_QTY"
}
