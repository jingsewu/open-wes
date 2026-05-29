import React, {useState, useEffect, useCallback} from "react"
import {useTranslation} from "react-i18next"
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    ArcElement,
    Tooltip,
    Legend,
    Filler,
} from "chart.js"
import {Doughnut, Line} from "react-chartjs-2"
import {Package, Truck, ArrowUpRight, Activity} from "lucide-react"
import request from "@/utils/requestInterceptor"

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    ArcElement,
    Tooltip,
    Legend,
    Filler
)

interface ProgressItem {
    name: string
    value: number
}

interface FlowData {
    inbound: number[]
    outbound: number[]
}

interface OperatorRow {
    operator: string
    totalQty: number
    acceptQty: number
    pickingQty: number
}

interface WorkstationRow {
    stationCode: string
    taskType: string
    taskCount: number
    requiredQty: number
    operatedQty: number
}

const CHART_COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#64748b"]

const cardStyle: React.CSSProperties = {
    background: "#fff",
    borderRadius: 12,
    padding: 24,
    boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
    transition: "box-shadow 0.2s ease, transform 0.2s ease",
}

// ── KPI Card ────────────────────────────────────────
function KpiCard({
    icon: Icon,
    iconBg,
    iconColor,
    title,
    value,
    suffix,
}: {
    icon: React.ElementType
    iconBg: string
    iconColor: string
    title: string
    value: string | number
    suffix?: string
}) {
    const [hovered, setHovered] = useState(false)
    return (
        <div
            style={{
                ...cardStyle,
                cursor: "default",
                ...(hovered
                    ? {boxShadow: "0 4px 12px rgba(0,0,0,0.08)", transform: "translateY(-1px)"}
                    : {}),
            }}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            <div style={{display: "flex", alignItems: "center", gap: 14}}>
                <div
                    style={{
                        width: 44,
                        height: 44,
                        borderRadius: 10,
                        background: iconBg,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        flexShrink: 0,
                    }}
                >
                    <Icon size={22} color={iconColor} />
                </div>
                <div>
                    <div style={{fontSize: 12, color: "#64748b", fontWeight: 500, marginBottom: 4}}>
                        {title}
                    </div>
                    <div style={{fontSize: 24, fontWeight: 700, color: "#1e293b", lineHeight: 1}}>
                        {value}
                        {suffix && (
                            <span style={{fontSize: 14, fontWeight: 500, color: "#94a3b8", marginLeft: 2}}>
                                {suffix}
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}

// ── Card wrapper ────────────────────────────────────
function DashCard({title, children, style: extra}: {title: string; children: React.ReactNode; style?: React.CSSProperties}) {
    return (
        <div style={{...cardStyle, ...extra}}>
            <div style={{fontSize: 14, fontWeight: 600, color: "#334155", marginBottom: 16}}>
                {title}
            </div>
            {children}
        </div>
    )
}

// ── Data table ──────────────────────────────────────
function DataTable<T extends Record<string, unknown>>({
    columns,
    data,
}: {
    columns: {key: string; label: string}[]
    data: T[]
}) {
    return (
        <div style={{overflowX: "auto"}}>
            <table style={{width: "100%", borderCollapse: "collapse", fontSize: 13}}>
                <thead>
                    <tr>
                        {columns.map((col) => (
                            <th
                                key={col.key}
                                style={{
                                    background: "#f1f5f9",
                                    padding: "10px 12px",
                                    textAlign: "left",
                                    fontWeight: 600,
                                    color: "#475569",
                                    borderBottom: "1px solid #e2e8f0",
                                    whiteSpace: "nowrap",
                                }}
                            >
                                {col.label}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.length === 0 ? (
                        <tr>
                            <td
                                colSpan={columns.length}
                                style={{padding: 24, textAlign: "center", color: "#94a3b8"}}
                            >
                                No data
                            </td>
                        </tr>
                    ) : (
                        data.map((row, idx) => (
                            <tr
                                key={idx}
                                style={{background: idx % 2 === 0 ? "#fff" : "#f8fafc"}}
                            >
                                {columns.map((col) => (
                                    <td
                                        key={col.key}
                                        style={{
                                            padding: "10px 12px",
                                            color: "#334155",
                                            borderBottom: "1px solid #f1f5f9",
                                        }}
                                    >
                                        {String(row[col.key] ?? "")}
                                    </td>
                                ))}
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    )
}

// ── Main Dashboard ──────────────────────────────────
export default function WmsDashboard() {
    const {t} = useTranslation()

    const [outboundProgress, setOutboundProgress] = useState<ProgressItem[]>([])
    const [inboundProgress, setInboundProgress] = useState<ProgressItem[]>([])
    const [flowData, setFlowData] = useState<FlowData>({inbound: [], outbound: []})
    const [operators, setOperators] = useState<OperatorRow[]>([])
    const [workstations, setWorkstations] = useState<WorkstationRow[]>([])

    const fetchOutbound = useCallback(async () => {
        try {
            const res = await request({method: "get", url: "/wms/api/dashboard/outbound-progress"})
            if (res?.data?.code === "0") {
                setOutboundProgress(
                    res.data.data.map((item: Record<string, unknown>) => ({
                        value: parseInt(item.value as string),
                        name: item.name as string,
                    }))
                )
            }
        } catch (err) {
            console.error("Failed to fetch outbound progress:", err)
        }
    }, [])

    const fetchInbound = useCallback(async () => {
        try {
            const res = await request({method: "get", url: "/wms/api/dashboard/inbound-progress"})
            if (res?.data?.code === "0") {
                setInboundProgress(
                    res.data.data.map((item: Record<string, unknown>) => ({
                        value: parseInt(item.value as string),
                        name: item.name as string,
                    }))
                )
            }
        } catch (err) {
            console.error("Failed to fetch inbound progress:", err)
        }
    }, [])

    const fetchFlow = useCallback(async () => {
        try {
            const res = await request({method: "get", url: "/wms/api/dashboard/hourly-flow"})
            if (res?.data) {
                const body = res.data
                setFlowData({
                    inbound: body.inbound || body.data?.inbound || [],
                    outbound: body.outbound || body.data?.outbound || [],
                })
            }
        } catch (err) {
            console.error("Failed to fetch hourly flow:", err)
        }
    }, [])

    const fetchOperators = useCallback(async () => {
        try {
            const res = await request({method: "get", url: "/wms/api/dashboard/operator-ranking"})
            if (res?.data) {
                const body = res.data
                setOperators(body.rows || body.data?.rows || body.items || body.data || [])
            }
        } catch (err) {
            console.error("Failed to fetch operator ranking:", err)
        }
    }, [])

    const fetchWorkstations = useCallback(async () => {
        try {
            const res = await request({method: "get", url: "/wms/api/dashboard/workstation-tasks"})
            if (res?.data) {
                const body = res.data
                setWorkstations(body.rows || body.data?.rows || body.items || body.data || [])
            }
        } catch (err) {
            console.error("Failed to fetch workstation tasks:", err)
        }
    }, [])

    useEffect(() => {
        fetchOutbound()
        fetchInbound()
        fetchFlow()
        fetchOperators()
        fetchWorkstations()

        const fast = setInterval(() => {
            fetchOutbound()
            fetchInbound()
            fetchWorkstations()
        }, 30000)

        const slow = setInterval(() => {
            fetchFlow()
            fetchOperators()
        }, 60000)

        return () => {
            clearInterval(fast)
            clearInterval(slow)
        }
    }, [fetchOutbound, fetchInbound, fetchFlow, fetchOperators, fetchWorkstations])

    // ── Computed values ─────────────────────────────
    const totalOrders = outboundProgress.reduce((sum, i) => sum + i.value, 0)

    const computeRate = (items: ProgressItem[]) => {
        const total = items.reduce((s, i) => s + i.value, 0)
        if (total === 0) return 0
        const completed = items
            .filter((i) => i.name?.toLowerCase().includes("complet") || i.name?.includes("完成"))
            .reduce((s, i) => s + i.value, 0)
        return Math.round((completed / total) * 100)
    }

    const inboundRate = computeRate(inboundProgress)
    const outboundRate = computeRate(outboundProgress)

    // ── Doughnut chart config ───────────────────────
    const makeDoughnutData = (items: ProgressItem[]) => ({
        labels: items.map((i) => i.name),
        datasets: [
            {
                data: items.map((i) => i.value),
                backgroundColor: CHART_COLORS.slice(0, items.length),
                borderWidth: 2,
                borderColor: "#fff",
                hoverOffset: 6,
            },
        ],
    })

    const doughnutOptions = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "65%",
        plugins: {
            legend: {
                position: "bottom" as const,
                labels: {font: {size: 11, family: "Plus Jakarta Sans"}, padding: 12, usePointStyle: true},
            },
        },
    }

    // ── Line chart config ───────────────────────────
    const hours = Array.from({length: 24}, (_, i) => String(i))

    const lineData = {
        labels: hours,
        datasets: [
            {
                label: t("dashboard.inbound"),
                data: flowData.inbound,
                borderColor: "#3b82f6",
                backgroundColor: "rgba(59, 130, 246, 0.08)",
                fill: true,
                tension: 0.4,
                pointRadius: 0,
                pointHoverRadius: 5,
                borderWidth: 2.5,
            },
            {
                label: t("dashboard.outbound"),
                data: flowData.outbound,
                borderColor: "#f59e0b",
                backgroundColor: "rgba(245, 158, 11, 0.08)",
                fill: true,
                tension: 0.4,
                pointRadius: 0,
                pointHoverRadius: 5,
                borderWidth: 2.5,
            },
        ],
    }

    const lineOptions = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {mode: "index" as const, intersect: false},
        plugins: {
            legend: {
                labels: {font: {size: 12, family: "Plus Jakarta Sans"}, usePointStyle: true, padding: 16},
            },
        },
        scales: {
            x: {grid: {display: false}, ticks: {font: {size: 11}}},
            y: {grid: {color: "#f1f5f9"}, ticks: {font: {size: 11}}},
        },
    }

    // ── Table columns ───────────────────────────────
    const operatorCols = [
        {key: "operator", label: t("dashboard.operator")},
        {key: "totalQty", label: t("dashboard.totalQty")},
        {key: "acceptQty", label: t("dashboard.acceptQty")},
        {key: "pickingQty", label: t("dashboard.pickingQty")},
    ]

    const workstationCols = [
        {key: "stationCode", label: t("dashboard.stationCode")},
        {key: "taskType", label: t("dashboard.taskType")},
        {key: "taskCount", label: t("dashboard.taskCount")},
        {key: "requiredQty", label: t("dashboard.requiredQty")},
        {key: "operatedQty", label: t("dashboard.pickingQty")},
    ]

    return (
        <div style={{padding: 24, maxWidth: 1400, margin: "0 auto"}}>
            {/* Page title */}
            <h2 style={{fontSize: 20, fontWeight: 700, color: "#1e293b", marginBottom: 20}}>
                {t("dashboard.title")}
            </h2>

            {/* KPI cards */}
            <div style={{display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16, marginBottom: 20}}>
                <KpiCard
                    icon={Package}
                    iconBg="#eff6ff"
                    iconColor="#3b82f6"
                    title={t("dashboard.todaysOrders")}
                    value={totalOrders.toLocaleString()}
                />
                <KpiCard
                    icon={Truck}
                    iconBg="#ecfdf5"
                    iconColor="#10b981"
                    title={t("dashboard.inboundProgress")}
                    value={inboundRate}
                    suffix="%"
                />
                <KpiCard
                    icon={ArrowUpRight}
                    iconBg="#fef3c7"
                    iconColor="#f59e0b"
                    title={t("dashboard.outboundProgress")}
                    value={outboundRate}
                    suffix="%"
                />
                <KpiCard
                    icon={Activity}
                    iconBg="#f0f9ff"
                    iconColor="#0ea5e9"
                    title={t("dashboard.activeWorkstations")}
                    value={workstations.length}
                />
            </div>

            {/* Charts */}
            <div style={{display: "grid", gridTemplateColumns: "1fr 1fr 2fr", gap: 16, marginBottom: 20}}>
                <DashCard title={t("dashboard.inboundProgress")}>
                    <div style={{height: 240}}>
                        {inboundProgress.length > 0 ? (
                            <Doughnut data={makeDoughnutData(inboundProgress)} options={doughnutOptions} />
                        ) : (
                            <div style={{display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#94a3b8"}}>
                                Loading...
                            </div>
                        )}
                    </div>
                </DashCard>

                <DashCard title={t("dashboard.outboundProgress")}>
                    <div style={{height: 240}}>
                        {outboundProgress.length > 0 ? (
                            <Doughnut data={makeDoughnutData(outboundProgress)} options={doughnutOptions} />
                        ) : (
                            <div style={{display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#94a3b8"}}>
                                Loading...
                            </div>
                        )}
                    </div>
                </DashCard>

                <DashCard title={t("dashboard.flowTitle")}>
                    <div style={{height: 240}}>
                        <Line data={lineData} options={lineOptions} />
                    </div>
                </DashCard>
            </div>

            {/* Tables */}
            <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16}}>
                <DashCard title={t("dashboard.operatorRanking")}>
                    <DataTable columns={operatorCols} data={operators} />
                </DashCard>

                <DashCard title={t("dashboard.workstationDetails")}>
                    <DataTable columns={workstationCols} data={workstations} />
                </DashCard>
            </div>
        </div>
    )
}
