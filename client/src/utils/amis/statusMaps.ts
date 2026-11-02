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
