import React, {useCallback, useEffect, useMemo, useRef, useState} from "react"
import {useTranslation} from "react-i18next"
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Tooltip,
} from "chart.js"
import type {Chart, ChartData, ChartOptions, Plugin, ScriptableContext, TooltipModel} from "chart.js"
import {Line} from "react-chartjs-2"
import {AlertTriangle, RefreshCw} from "lucide-react"
import request from "@/utils/requestInterceptor"
import "@/scss/dashboard.scss"

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip)

// ─── Data ─────────────────────────────────────────────

const API = {
    inbound: "/wms/api/dashboard/inbound-progress",
    outbound: "/wms/api/dashboard/outbound-progress",
    flow: "/wms/api/dashboard/hourly-flow",
    operators: "/wms/api/dashboard/operator-ranking",
    workstations: "/wms/api/dashboard/workstation-tasks",
}

interface ApiEnvelope<T> {
    code?: string
    data?: T
}

interface ResponseLike<T> {
    data?: ApiEnvelope<T>
}

interface StatusRow {
    name: string
    value: number
}

interface FlowData {
    inbound: number[]
    outbound: number[]
}

interface OperatorRow {
    account: string
    operator: string
    totalQty: number
    putAwayQty: number
    pickingQty: number
}

interface WorkstationRow {
    stationCode: string
    taskType: string
    taskCount: number
    requiredQty: number
    operatedQty: number
}

type Settled<T> = {ok: true; value: T} | {ok: false}

function settle<T>(promise: Promise<T>): Promise<Settled<T>> {
    return promise
        .then((value) => ({ok: true as const, value}))
        .catch(() => ({ok: false as const}))
}

async function fetchData<T>(url: string): Promise<T> {
    const res = (await request({method: "get", url})) as ResponseLike<T>
    if (res?.data?.code !== "0") {
        throw new Error(`Dashboard request failed: ${url}`)
    }
    return res.data.data as T
}

async function fetchRows(url: string): Promise<Record<string, unknown>[]> {
    const payload = await fetchData<unknown>(url)
    return Array.isArray(payload) ? (payload as Record<string, unknown>[]) : []
}

function toNumber(value: unknown): number {
    const parsed = Number(value)
    return isFinite(parsed) ? parsed : 0
}

function toText(value: unknown): string {
    return value === null || value === undefined ? "" : String(value)
}

function fetchStatuses(url: string): Promise<StatusRow[]> {
    return fetchRows(url).then((rows) =>
        rows.map((row) => ({name: toText(row.name), value: toNumber(row.value)}))
    )
}

function fetchFlow(): Promise<FlowData> {
    return fetchData<{inbound?: unknown; outbound?: unknown}>(API.flow).then((payload) => {
        const series = (value: unknown): number[] =>
            Array.isArray(value) ? value.map(toNumber) : []
        return {
            inbound: series(payload && payload.inbound),
            outbound: series(payload && payload.outbound),
        }
    })
}

function fetchOperators(): Promise<OperatorRow[]> {
    return fetchRows(API.operators).then((rows) =>
        rows.map((row) => {
            const operator = toText(row.operator)
            return {
                // the account is the stable identity; fall back for older responses
                account: toText(row.account) || operator,
                operator: operator || toText(row.account),
                totalQty: toNumber(row.totalQty),
                putAwayQty: toNumber(row.putAwayQty !== undefined ? row.putAwayQty : row.acceptQty),
                pickingQty: toNumber(row.pickingQty),
            }
        })
    )
}

function fetchWorkstations(): Promise<WorkstationRow[]> {
    return fetchRows(API.workstations).then((rows) =>
        rows
            .map((row) => ({
                stationCode: toText(row.stationCode),
                taskType: toText(row.taskType),
                taskCount: toNumber(row.taskCount),
                requiredQty: toNumber(row.requiredQty),
                operatedQty: toNumber(row.operatedQty),
            }))
            .filter((row) => row.stationCode.length > 0)
    )
}

// ─── Status model ─────────────────────────────────────

interface StatusGroup {
    key: string
    labelKey: string
    color: string
    statuses: string[]
}

interface Segment {
    key: string
    label: string
    color: string
    value: number
}

// One-hue ordinal ramp (validated against white): later stages sit darker.
// Canceled orders are an off-ramp and take a neutral gray.
const INBOUND_GROUPS: StatusGroup[] = [
    {key: "new", labelKey: "dashboard.statusNew", color: "#86b6ef", statuses: ["NEW"]},
    {key: "receiving", labelKey: "dashboard.statusReceiving", color: "#5598e7", statuses: ["ACCEPTING"]},
    {key: "received", labelKey: "dashboard.statusReceived", color: "#2a78d6", statuses: ["ACCEPTED"]},
    {key: "closed", labelKey: "dashboard.statusClosed", color: "#1c5cab", statuses: ["CLOSED"]},
    {key: "canceled", labelKey: "dashboard.statusCanceled", color: "#94a3b8", statuses: ["CANCEL"]},
]

const OUTBOUND_GROUPS: StatusGroup[] = [
    {key: "new", labelKey: "dashboard.statusNew", color: "#86b6ef", statuses: ["NEW"]},
    {key: "short-waiting", labelKey: "dashboard.statusShortWaiting", color: "#5598e7", statuses: ["SHORT_WAITING"]},
    {key: "allocated", labelKey: "dashboard.statusAllocated", color: "#2a78d6", statuses: ["ASSIGNED", "DISPATCHED"]},
    {key: "picking", labelKey: "dashboard.statusPicking", color: "#1c5cab", statuses: ["PICKING"]},
    {key: "picked", labelKey: "dashboard.statusPicked", color: "#104281", statuses: ["PICKED"]},
    {key: "canceled", labelKey: "dashboard.statusCanceled", color: "#94a3b8", statuses: ["CANCELED"]},
]

const INBOUND_DONE = ["ACCEPTED", "CLOSED"]
const OUTBOUND_DONE = ["PICKED"]

function countStatuses(rows: StatusRow[], statuses: string[]): number {
    return rows.reduce(
        (sum, row) => (statuses.indexOf(row.name.toUpperCase()) >= 0 ? sum + row.value : sum),
        0
    )
}

function prettifyStatus(name: string): string {
    return name
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/^./, (first) => first.toUpperCase())
}

function buildSegments(
    rows: StatusRow[],
    groups: StatusGroup[],
    translate: (key: string) => string
): Segment[] {
    const known: string[] = []
    const segments: Segment[] = []

    groups.forEach((group) => {
        group.statuses.forEach((status) => known.push(status))
        const value = countStatuses(rows, group.statuses)
        if (value > 0) {
            segments.push({
                key: group.key,
                label: translate(group.labelKey),
                color: group.color,
                value,
            })
        }
    })

    const unknown: {[name: string]: number} = {}
    rows.forEach((row) => {
        const name = row.name.toUpperCase()
        if (known.indexOf(name) < 0) {
            unknown[name] = (unknown[name] || 0) + row.value
        }
    })
    Object.keys(unknown).forEach((name) => {
        if (unknown[name] > 0) {
            segments.push({
                key: `other-${name}`,
                label: name ? prettifyStatus(name) : translate("dashboard.statusOther"),
                color: "#94a3b8",
                value: unknown[name],
            })
        }
    })

    return segments
}

// ─── Chart setup ──────────────────────────────────────

const SERIES_INBOUND = "#2563eb"
const SERIES_OUTBOUND = "#d97706"
const FONT = "'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
const PREFERS_REDUCED_MOTION =
    typeof window !== "undefined" &&
    !!window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches

type TooltipContext = {chart: Chart; tooltip: TooltipModel<"line">}

function clamp(value: number, min: number, max: number): number {
    if (max < min) {
        return min
    }
    return Math.min(Math.max(value, min), max)
}

const crosshair: Plugin<"line"> = {
    id: "dashCrosshair",
    afterDatasetsDraw(chart) {
        const tooltip = chart.tooltip
        const active = tooltip ? tooltip.getActiveElements() : []
        if (active.length === 0) {
            return
        }
        const area = chart.chartArea
        const x = active[0].element.x
        const ctx = chart.ctx
        ctx.save()
        ctx.beginPath()
        ctx.moveTo(x, area.top)
        ctx.lineTo(x, area.bottom)
        ctx.lineWidth = 1
        ctx.strokeStyle = "rgba(71, 85, 105, 0.35)"
        ctx.stroke()
        ctx.restore()
    },
}

function pad2(value: number): string {
    return (value < 10 ? "0" : "") + value
}

function compactNumber(value: number): string {
    if (value >= 10000) {
        return `${Math.round(value / 1000)}K`
    }
    if (value >= 1000) {
        return `${(value / 1000).toFixed(1)}K`
    }
    return String(value)
}

function useNumberFormat(): (value: number) => string {
    const {i18n} = useTranslation()
    return useMemo(() => {
        const formatter = new Intl.NumberFormat(i18n.language)
        return (value: number) => formatter.format(value)
    }, [i18n.language])
}

// ─── Pieces ───────────────────────────────────────────

function ProgressBlock({
    title,
    totalCount,
    rate,
    segments,
    emptyText,
    loading,
}: {
    title: string
    totalCount: number
    rate: number | null
    segments: Segment[]
    emptyText: string
    loading: boolean
}) {
    const {t} = useTranslation()
    const formatNumber = useNumberFormat()

    return (
        <div className="dash-progress">
            <div className="dash-progress__top">
                <div>
                    <h3 className="dash-progress__name">{title}</h3>
                    <p className="dash-progress__count">
                        {loading ? "—" : t("dashboard.orderCount", {value: formatNumber(totalCount)})}
                    </p>
                </div>
                <div className="dash-progress__rate">
                    <b>{rate === null ? "—" : `${rate}%`}</b>
                    <span>{t("dashboard.complete")}</span>
                </div>
            </div>

            {segments.length > 0 ? (
                <React.Fragment>
                    <div className="dash-track" aria-hidden="true">
                        {segments.map((segment) => (
                            <span
                                key={segment.key}
                                className="dash-track__seg"
                                style={{flexGrow: segment.value, background: segment.color}}
                            />
                        ))}
                    </div>
                    <ul className="dash-status">
                        {segments.map((segment) => (
                            <li key={segment.key}>
                                <span
                                    className="dash-status__key"
                                    style={{background: segment.color}}
                                    aria-hidden="true"
                                />
                                <span className="dash-status__label">{segment.label}</span>
                                <span className="dash-status__value">{formatNumber(segment.value)}</span>
                            </li>
                        ))}
                    </ul>
                </React.Fragment>
            ) : (
                <p className="dash-empty dash-empty--rail">
                    {loading ? t("dashboard.loading") : emptyText}
                </p>
            )}
        </div>
    )
}

function FlowTable({
    labels,
    inbound,
    outbound,
}: {
    labels: string[]
    inbound: number[]
    outbound: number[]
}) {
    const {t} = useTranslation()
    const formatNumber = useNumberFormat()

    return (
        <table className="dash-table">
            <thead>
                <tr>
                    <th>{t("dashboard.hour")}</th>
                    <th className="is-num">{t("dashboard.inbound")}</th>
                    <th className="is-num">{t("dashboard.outbound")}</th>
                </tr>
            </thead>
            <tbody>
                {labels.map((label, index) => (
                    <tr key={label}>
                        <td>{label}</td>
                        <td className="is-num">{formatNumber(inbound[index] || 0)}</td>
                        <td className="is-num">{formatNumber(outbound[index] || 0)}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    )
}

function OperatorTable({rows, loading}: {rows: OperatorRow[]; loading: boolean}) {
    const {t} = useTranslation()
    const formatNumber = useNumberFormat()

    if (rows.length === 0) {
        return (
            <p className="dash-empty dash-empty--table">
                {loading ? t("dashboard.loading") : t("dashboard.noOperators")}
            </p>
        )
    }

    return (
        <div className="dash-table-wrap dash-table-wrap--scroll">
            <table className="dash-table dash-table--operators">
                <thead>
                    <tr>
                        <th>{t("dashboard.operator")}</th>
                        <th className="is-num">{t("dashboard.totalQty")}</th>
                        <th className="is-num">{t("dashboard.putAwayQty")}</th>
                        <th className="is-num">{t("dashboard.pickingQty")}</th>
                    </tr>
                </thead>
                <tbody>
                    {rows.map((row, index) => (
                        <tr key={row.account}>
                            <td>
                                <span className="dash-rank">{pad2(index + 1)}</span>
                                <span className="dash-operator">{row.operator}</span>
                            </td>
                            <td className="is-num">{formatNumber(row.totalQty)}</td>
                            <td className="is-num">{formatNumber(row.putAwayQty)}</td>
                            <td className="is-num">{formatNumber(row.pickingQty)}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}

const TASK_LABELS: {[taskType: string]: string} = {
    PUT_AWAY: "dashboard.taskPutAway",
    PICKING: "dashboard.taskPicking",
    TWO_STEP_RELOCATION: "dashboard.taskTwoStep",
    ONE_STEP_RELOCATION: "dashboard.taskOneStep",
    STOCK_TAKE: "dashboard.taskStockTake",
    RECHECK: "dashboard.taskRecheck",
    ADJUST: "dashboard.taskAdjust",
}

function WorkstationTable({rows, loading}: {rows: WorkstationRow[]; loading: boolean}) {
    const {t} = useTranslation()
    const formatNumber = useNumberFormat()

    if (rows.length === 0) {
        return (
            <p className="dash-empty dash-empty--table">
                {loading ? t("dashboard.loading") : t("dashboard.noStations")}
            </p>
        )
    }

    return (
        <div className="dash-table-wrap dash-table-wrap--scroll">
            <table className="dash-table dash-table--stations">
                <thead>
                    <tr>
                        <th>{t("dashboard.stationCode")}</th>
                        <th>{t("dashboard.taskType")}</th>
                        <th className="is-num">{t("dashboard.taskCount")}</th>
                        <th className="is-num">{t("dashboard.requiredQty")}</th>
                        <th className="is-num">{t("dashboard.operatedQty")}</th>
                    </tr>
                </thead>
                <tbody>
                    {rows.map((row) => {
                        const labelKey = TASK_LABELS[row.taskType.toUpperCase()]
                        return (
                            <tr key={`${row.stationCode}-${row.taskType}`}>
                                <td>
                                    <span className="dash-station">{row.stationCode}</span>
                                </td>
                                <td>
                                    <span className="dash-task">
                                        {labelKey ? t(labelKey) : prettifyStatus(row.taskType)}
                                    </span>
                                </td>
                                <td className="is-num">{formatNumber(row.taskCount)}</td>
                                <td className="is-num">{formatNumber(row.requiredQty)}</td>
                                <td className="is-num">{formatNumber(row.operatedQty)}</td>
                            </tr>
                        )
                    })}
                </tbody>
            </table>
        </div>
    )
}

// ─── Page ─────────────────────────────────────────────

const RAIL_PORTS = [12.5, 37.5, 62.5, 87.5]

export default function WmsDashboard() {
    const {t, i18n} = useTranslation()
    const formatNumber = useNumberFormat()

    const [inboundRows, setInboundRows] = useState<StatusRow[]>([])
    const [outboundRows, setOutboundRows] = useState<StatusRow[]>([])
    const [flow, setFlow] = useState<FlowData>({inbound: [], outbound: []})
    const [operators, setOperators] = useState<OperatorRow[]>([])
    const [workstations, setWorkstations] = useState<WorkstationRow[]>([])
    const [loading, setLoading] = useState(true)
    const [refreshing, setRefreshing] = useState(false)
    const [hasError, setHasError] = useState(false)
    const [updatedAt, setUpdatedAt] = useState<Date | null>(null)
    const [view, setView] = useState<"chart" | "table">("chart")

    const aliveRef = useRef(true)
    const busyRef = useRef(false)

    const loadAll = useCallback(async (manual?: boolean) => {
        if (busyRef.current) {
            return
        }
        busyRef.current = true
        if (manual) {
            setRefreshing(true)
        }

        const results = await Promise.all([
            settle(fetchStatuses(API.inbound)),
            settle(fetchStatuses(API.outbound)),
            settle(fetchFlow()),
            settle(fetchOperators()),
            settle(fetchWorkstations()),
        ])

        if (!aliveRef.current) {
            busyRef.current = false
            return
        }

        const [inbound, outbound, flowResult, operatorResult, workstationResult] = results
        if (inbound.ok) {
            setInboundRows(inbound.value)
        }
        if (outbound.ok) {
            setOutboundRows(outbound.value)
        }
        if (flowResult.ok) {
            setFlow(flowResult.value)
        }
        if (operatorResult.ok) {
            setOperators(operatorResult.value)
        }
        if (workstationResult.ok) {
            setWorkstations(workstationResult.value)
        }

        const failures = results.filter((result) => !result.ok).length
        setHasError(failures > 0)
        if (failures < results.length) {
            setUpdatedAt(new Date())
        }
        setLoading(false)
        setRefreshing(false)
        busyRef.current = false
    }, [])

    useEffect(() => {
        aliveRef.current = true
        void loadAll()
        const timer = window.setInterval(() => {
            void loadAll()
        }, 30000)
        return () => {
            aliveRef.current = false
            window.clearInterval(timer)
        }
    }, [loadAll])

    // ── KPIs ──────────────────────────────────────────
    const inboundTotal = useMemo(
        () => inboundRows.reduce((sum, row) => sum + row.value, 0),
        [inboundRows]
    )
    const outboundTotal = useMemo(
        () => outboundRows.reduce((sum, row) => sum + row.value, 0),
        [outboundRows]
    )
    const receivingNow = useMemo(() => countStatuses(inboundRows, ["ACCEPTING"]), [inboundRows])
    const pickingNow = useMemo(() => countStatuses(outboundRows, ["PICKING"]), [outboundRows])
    const activeStations = useMemo(() => {
        const seen: {[code: string]: boolean} = {}
        let count = 0
        workstations.forEach((row) => {
            if (!seen[row.stationCode]) {
                seen[row.stationCode] = true
                count += 1
            }
        })
        return count
    }, [workstations])

    // ── Progress ──────────────────────────────────────
    const inboundDone = countStatuses(inboundRows, INBOUND_DONE)
    const inboundRate = inboundTotal > 0 ? Math.round((inboundDone / inboundTotal) * 100) : null
    const inboundSegments = useMemo(
        () => buildSegments(inboundRows, INBOUND_GROUPS, (key) => String(t(key))),
        [inboundRows, t]
    )

    const outboundDone = countStatuses(outboundRows, OUTBOUND_DONE)
    const outboundRate = outboundTotal > 0 ? Math.round((outboundDone / outboundTotal) * 100) : null
    const outboundSegments = useMemo(
        () => buildSegments(outboundRows, OUTBOUND_GROUPS, (key) => String(t(key))),
        [outboundRows, t]
    )

    // ── Flow ──────────────────────────────────────────
    const flowHours = useMemo(() => {
        const nowHour = new Date().getHours()
        let last = nowHour
        flow.inbound.forEach((value, index) => {
            if (value > 0 && index > last) {
                last = index
            }
        })
        flow.outbound.forEach((value, index) => {
            if (value > 0 && index > last) {
                last = index
            }
        })
        return Math.min(last, 23)
    }, [flow])

    const hourLabels = useMemo(() => {
        const labels: string[] = []
        for (let hour = 0; hour <= flowHours; hour++) {
            labels.push(`${pad2(hour)}:00`)
        }
        return labels
    }, [flowHours])

    const inboundSeries = useMemo(() => flow.inbound.slice(0, flowHours + 1), [flow, flowHours])
    const outboundSeries = useMemo(() => flow.outbound.slice(0, flowHours + 1), [flow, flowHours])
    const hasFlow = useMemo(
        () => inboundSeries.some((value) => value > 0) || outboundSeries.some((value) => value > 0),
        [inboundSeries, outboundSeries]
    )
    const inboundFlowTotal = useMemo(
        () => inboundSeries.reduce((sum, value) => sum + value, 0),
        [inboundSeries]
    )
    const outboundFlowTotal = useMemo(
        () => outboundSeries.reduce((sum, value) => sum + value, 0),
        [outboundSeries]
    )

    const externalTooltip = useCallback(
        (context: TooltipContext) => {
            const {chart, tooltip} = context
            const host = chart.canvas.parentNode
            if (!(host instanceof HTMLElement)) {
                return
            }

            let element = host.querySelector<HTMLDivElement>(".dash-tip")
            if (!element) {
                element = document.createElement("div")
                element.className = "dash-tip"
                host.appendChild(element)
            }

            if (tooltip.opacity === 0) {
                element.style.opacity = "0"
                return
            }

            element.textContent = ""
            const hour = document.createElement("div")
            hour.className = "dash-tip__hour"
            hour.textContent = tooltip.title.length > 0 ? tooltip.title[0] : ""
            element.appendChild(hour)

            const points = tooltip.dataPoints || []
            points.forEach((point) => {
                const row = document.createElement("div")
                row.className = "dash-tip__row"

                const key = document.createElement("span")
                key.className = "dash-tip__key"
                key.style.background = String(point.dataset.borderColor || "transparent")

                const label = document.createElement("span")
                label.className = "dash-tip__label"
                label.textContent = String(point.dataset.label || "")

                const value = document.createElement("b")
                value.textContent = formatNumber(Number(point.parsed.y))

                row.appendChild(key)
                row.appendChild(label)
                row.appendChild(value)
                element.appendChild(row)
            })

            const half = element.offsetWidth / 2
            const left = clamp(
                chart.canvas.offsetLeft + tooltip.caretX,
                half + 4,
                host.clientWidth - half - 4
            )
            element.style.left = `${left}px`
            element.style.top = `${chart.canvas.offsetTop + tooltip.caretY - 12}px`
            element.style.opacity = "1"
        },
        [formatNumber]
    )

    const lineData = useMemo<ChartData<"line", number[], string>>(
        () => ({
            labels: hourLabels,
            datasets: [
                {
                    label: t("dashboard.inbound"),
                    data: inboundSeries,
                    borderColor: SERIES_INBOUND,
                    fill: false,
                    tension: 0.35,
                    borderWidth: 2,
                    borderCapStyle: "round",
                    borderJoinStyle: "round",
                    pointRadius: (context: ScriptableContext<"line">) =>
                        context.dataIndex === inboundSeries.length - 1 ? 4 : 0,
                    pointHoverRadius: 5,
                    pointHitRadius: 12,
                    pointBackgroundColor: SERIES_INBOUND,
                    pointBorderColor: "#ffffff",
                    pointBorderWidth: 2,
                },
                {
                    label: t("dashboard.outbound"),
                    data: outboundSeries,
                    borderColor: SERIES_OUTBOUND,
                    fill: false,
                    tension: 0.35,
                    borderWidth: 2,
                    borderCapStyle: "round",
                    borderJoinStyle: "round",
                    pointRadius: (context: ScriptableContext<"line">) =>
                        context.dataIndex === outboundSeries.length - 1 ? 4 : 0,
                    pointHoverRadius: 5,
                    pointHitRadius: 12,
                    pointBackgroundColor: SERIES_OUTBOUND,
                    pointBorderColor: "#ffffff",
                    pointBorderWidth: 2,
                },
            ],
        }),
        [hourLabels, inboundSeries, outboundSeries, t]
    )

    const lineOptions = useMemo<ChartOptions<"line">>(
        () => ({
            responsive: true,
            maintainAspectRatio: false,
            animation: PREFERS_REDUCED_MOTION ? false : {duration: 450},
            interaction: {mode: "index", intersect: false},
            layout: {padding: {top: 6, right: 6}},
            plugins: {
                legend: {display: false},
                tooltip: {
                    // enabled: false suppresses Chart.js's own tooltip;
                    // the external renderer still receives hover updates.
                    enabled: false,
                    external: externalTooltip,
                    mode: "index",
                    intersect: false,
                },
            },
            scales: {
                x: {
                    border: {display: false},
                    grid: {display: false},
                    ticks: {
                        color: "#64748b",
                        font: {size: 11, family: FONT},
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 8,
                    },
                },
                y: {
                    beginAtZero: true,
                    border: {display: false},
                    grid: {color: "#eef2f7"},
                    ticks: {
                        color: "#64748b",
                        font: {size: 11, family: FONT},
                        maxTicksLimit: 5,
                        callback: (value: string | number) => compactNumber(Number(value)),
                    },
                },
            },
        }),
        [externalTooltip]
    )

    const dateLabel = new Intl.DateTimeFormat(i18n.language, {
        weekday: "long",
        day: "numeric",
        month: "long",
    }).format(new Date())
    const updatedLabel = updatedAt
        ? updatedAt.toLocaleTimeString(i18n.language, {
              hour: "2-digit",
              minute: "2-digit",
              hour12: false,
          })
        : ""

    return (
        <div className="dashboard-page">
            <div className="dashboard-page__inner">
                <section className="dash-band" aria-labelledby="dash-page-title">
                    <div className="dash-band__top">
                        <div>
                            <h1 id="dash-page-title" className="dash-band__title">
                                {t("dashboard.floorTitle")}
                            </h1>
                            <p className="dash-band__date">{dateLabel}</p>
                        </div>
                        <div className="dash-band__tools">
                            <span className={hasError ? "dash-live dash-live--error" : "dash-live"}>
                                <span className="dash-live__dot" aria-hidden="true" />
                                {t("dashboard.live")}
                            </span>
                            {updatedLabel ? (
                                <span className="dash-band__updated">
                                    {t("dashboard.updated")}{" "}
                                    <span className="dash-band__time">{updatedLabel}</span>
                                </span>
                            ) : null}
                            <button
                                type="button"
                                className={
                                    refreshing ? "dash-refresh dash-refresh--busy" : "dash-refresh"
                                }
                                onClick={() => {
                                    void loadAll(true)
                                }}
                                disabled={refreshing}
                                aria-label={t("dashboard.refresh")}
                                title={t("dashboard.refresh")}
                            >
                                <RefreshCw aria-hidden="true" />
                            </button>
                        </div>
                    </div>

                    {hasError ? (
                        <div className="dash-band__alert" role="status">
                            <AlertTriangle size={14} aria-hidden="true" />
                            <span>{t("dashboard.loadError")}</span>
                            <button
                                type="button"
                                onClick={() => {
                                    void loadAll(true)
                                }}
                            >
                                {t("dashboard.retry")}
                            </button>
                        </div>
                    ) : null}

                    <div className="dash-kpis">
                        <div className="dash-kpi">
                            <p className="dash-kpi__label">{t("dashboard.ordersToday")}</p>
                            <p className="dash-kpi__value">
                                {loading ? "—" : formatNumber(inboundTotal + outboundTotal)}
                            </p>
                            <p className="dash-kpi__sub">{t("dashboard.sinceMidnight")}</p>
                        </div>
                        <div className="dash-kpi">
                            <p className="dash-kpi__label">{t("dashboard.receivingNow")}</p>
                            <p className="dash-kpi__value">
                                {loading ? "—" : formatNumber(receivingNow)}
                            </p>
                            <p className="dash-kpi__sub">
                                {loading
                                    ? "—"
                                    : t("dashboard.receivingNowSub", {
                                          value: formatNumber(inboundTotal),
                                      })}
                            </p>
                        </div>
                        <div className="dash-kpi">
                            <p className="dash-kpi__label">{t("dashboard.pickingNow")}</p>
                            <p className="dash-kpi__value">
                                {loading ? "—" : formatNumber(pickingNow)}
                            </p>
                            <p className="dash-kpi__sub">
                                {loading
                                    ? "—"
                                    : t("dashboard.pickingNowSub", {
                                          value: formatNumber(outboundTotal),
                                      })}
                            </p>
                        </div>
                        <div className="dash-kpi">
                            <p className="dash-kpi__label">{t("dashboard.activeStations")}</p>
                            <p className="dash-kpi__value">
                                {loading ? "—" : formatNumber(activeStations)}
                            </p>
                            <p className="dash-kpi__sub">{t("dashboard.activeStationsSub")}</p>
                        </div>
                    </div>

                    <div className="dash-rail" aria-hidden="true">
                        <div className="dash-rail__line" />
                        {RAIL_PORTS.map((position) => (
                            <span
                                key={position}
                                className="dash-rail__port"
                                style={{left: `${position}%`}}
                            />
                        ))}
                    </div>
                </section>

                <div className="dash-grid">
                    <section className="dash-panel dash-flow" aria-labelledby="dash-flow-title">
                        <div className="dash-panel__head">
                            <div>
                                <h2 id="dash-flow-title" className="dash-panel__title">
                                    {t("dashboard.flowTitle")}
                                </h2>
                                <p className="dash-panel__sub">{t("dashboard.flowSub")}</p>
                            </div>
                            <div className="dash-seg" role="group" aria-label={t("dashboard.view")}>
                                <button
                                    type="button"
                                    aria-pressed={view === "chart"}
                                    onClick={() => setView("chart")}
                                >
                                    {t("dashboard.chartView")}
                                </button>
                                <button
                                    type="button"
                                    aria-pressed={view === "table"}
                                    onClick={() => setView("table")}
                                >
                                    {t("dashboard.tableView")}
                                </button>
                            </div>
                        </div>

                        <div className="dash-series">
                            <span className="dash-series__item">
                                <span
                                    className="dash-series__key"
                                    style={{background: SERIES_INBOUND}}
                                    aria-hidden="true"
                                />
                                {t("dashboard.inbound")}
                                <span className="dash-series__value">
                                    {formatNumber(inboundFlowTotal)}
                                </span>
                            </span>
                            <span className="dash-series__item">
                                <span
                                    className="dash-series__key"
                                    style={{background: SERIES_OUTBOUND}}
                                    aria-hidden="true"
                                />
                                {t("dashboard.outbound")}
                                <span className="dash-series__value">
                                    {formatNumber(outboundFlowTotal)}
                                </span>
                            </span>
                        </div>

                        {view === "chart" ? (
                            <div className="dash-flow__plot">
                                {hasFlow ? (
                                    <Line
                                        data={lineData}
                                        options={lineOptions}
                                        plugins={[crosshair]}
                                    />
                                ) : (
                                    <div className="dash-empty">
                                        {loading ? t("dashboard.loading") : t("dashboard.noFlow")}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="dash-flow__table">
                                {hasFlow ? (
                                    <FlowTable
                                        labels={hourLabels}
                                        inbound={inboundSeries}
                                        outbound={outboundSeries}
                                    />
                                ) : (
                                    <p className="dash-empty dash-empty--table">
                                        {loading ? t("dashboard.loading") : t("dashboard.noFlow")}
                                    </p>
                                )}
                            </div>
                        )}
                    </section>

                    <section className="dash-panel dash-progress-panel" aria-label={t("dashboard.progressTitle")}>
                        <ProgressBlock
                            title={t("dashboard.inboundOrders")}
                            totalCount={inboundTotal}
                            rate={inboundRate}
                            segments={inboundSegments}
                            emptyText={t("dashboard.noInbound")}
                            loading={loading}
                        />
                        <ProgressBlock
                            title={t("dashboard.outboundOrders")}
                            totalCount={outboundTotal}
                            rate={outboundRate}
                            segments={outboundSegments}
                            emptyText={t("dashboard.noOutbound")}
                            loading={loading}
                        />
                    </section>
                </div>

                <div className="dash-grid dash-grid--tables">
                    <section className="dash-panel" aria-labelledby="dash-operators-title">
                        <div className="dash-panel__head">
                            <div>
                                <h2 id="dash-operators-title" className="dash-panel__title">
                                    {t("dashboard.operatorRanking")}
                                </h2>
                                <p className="dash-panel__sub">
                                    {t("dashboard.operatorSub", {
                                        value: formatNumber(operators.length),
                                    })}
                                </p>
                            </div>
                        </div>
                        <OperatorTable rows={operators} loading={loading} />
                    </section>

                    <section className="dash-panel" aria-labelledby="dash-stations-title">
                        <div className="dash-panel__head">
                            <div>
                                <h2 id="dash-stations-title" className="dash-panel__title">
                                    {t("dashboard.workstationDetails")}
                                </h2>
                                <p className="dash-panel__sub">{t("dashboard.workstationSub")}</p>
                            </div>
                        </div>
                        <WorkstationTable rows={workstations} loading={loading} />
                    </section>
                </div>
            </div>
        </div>
    )
}
