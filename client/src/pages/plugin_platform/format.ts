import dayjs from "dayjs"

export function formatMillis(value: string | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const millis = Number(value)
    if (!isFinite(millis)) {
        return "-"
    }
    return dayjs(millis).format("YYYY-MM-DD HH:mm:ss")
}

export function formatDuration(value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const millis = Number(value)
    if (!isFinite(millis)) {
        return "-"
    }
    if (millis < 1000) {
        return millis + " ms"
    }
    return (millis / 1000).toFixed(2) + " s"
}

export function formatPercent(value: number | null | undefined): string {
    if (value === null || value === undefined || !isFinite(value)) {
        return "-"
    }
    return value.toFixed(2) + "%"
}

export function formatNumber(value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const numeric = Number(value)
    return isFinite(numeric) ? String(numeric) : "-"
}
