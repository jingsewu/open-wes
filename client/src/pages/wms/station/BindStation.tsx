import React, { memo, useEffect, useState } from "react"
import { Button, Spin, Alert } from "antd"
import { useTranslation } from "react-i18next"
import store from "@/stores"
import { request_work_station } from "@/pages/wms/station/constants/constant"

interface BindStationProps {
    onStationSelected: (id: string) => void
}

const BindStation = ({ onStationSelected }: BindStationProps) => {
    const { t } = useTranslation()
    const [selectedId, setSelectedId] = useState("")
    const [options, setOptions] = useState<any[]>([])
    const [loading, setLoading] = useState(false)
    const [fetchError, setFetchError] = useState("")

    useEffect(() => {
        let isMounted = true

        const fetchStations = async () => {
            try {
                setLoading(true)
                setFetchError("")
                const res: any = await request_work_station(store.warehouse.code)
                if (!isMounted) return
                setOptions(res?.data?.items || [])
            } catch (error: any) {
                if (!isMounted) return
                setFetchError(error?.message || t("station.bind.fetchError"))
            } finally {
                if (isMounted) setLoading(false)
            }
        }

        fetchStations()
        return () => {
            isMounted = false
        }
    }, [store.warehouse.code])

    return (
        <div
            style={{
                width: "100%",
                height: "100%",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                backgroundColor: "#f9fafb"
            }}
        >
            <div
                style={{
                    width: "100%",
                    maxWidth: 420,
                    display: "flex",
                    flexDirection: "column",
                    gap: 20
                }}
            >
                <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 22, fontWeight: 700, color: "#111827" }}>
                        {t("station.bind.title")}
                    </div>
                    <div style={{ fontSize: 13, color: "#6b7280", marginTop: 6 }}>
                        {t("station.bind.subtitle")}
                    </div>
                </div>

                {fetchError && (
                    <Alert
                        message={t("station.bind.fetchError")}
                        description={fetchError}
                        type="error"
                        showIcon
                        action={
                            <Button size="small" onClick={() => window.location.reload()}>
                                {t("station.bind.retry")}
                            </Button>
                        }
                    />
                )}

                <Spin spinning={loading}>
                    <div
                        style={{
                            background: "#fff",
                            border: "1px solid #e5e7eb",
                            borderRadius: 10,
                            overflow: "hidden",
                            boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
                            minHeight: 48
                        }}
                    >
                        {options.length === 0 && !loading && (
                            <div
                                style={{
                                    padding: "24px 16px",
                                    textAlign: "center",
                                    color: "#9ca3af",
                                    fontSize: 13
                                }}
                            >
                                {t("station.bind.noStations")}
                            </div>
                        )}
                        {options.map((station, i) => (
                            <div
                                key={station.id}
                                onClick={() => setSelectedId(station.id)}
                                style={{
                                    padding: "12px 16px",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "space-between",
                                    cursor: "pointer",
                                    borderTop: i > 0 ? "1px solid #f3f4f6" : undefined,
                                    background:
                                        selectedId === station.id ? "#eff6ff" : "#fff",
                                    borderLeft:
                                        selectedId === station.id
                                            ? "3px solid #2563eb"
                                            : "3px solid transparent",
                                    transition: "background 0.15s"
                                }}
                            >
                                <div
                                    style={{
                                        fontWeight: selectedId === station.id ? 600 : 500,
                                        fontSize: 14,
                                        color:
                                            selectedId === station.id
                                                ? "#1e40af"
                                                : "#374151"
                                    }}
                                >
                                    {station.stationName}
                                </div>
                                <div
                                    style={{
                                        width: 18,
                                        height: 18,
                                        borderRadius: "50%",
                                        background:
                                            selectedId === station.id
                                                ? "#2563eb"
                                                : "transparent",
                                        border:
                                            selectedId === station.id
                                                ? "none"
                                                : "2px solid #d1d5db",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        flexShrink: 0
                                    }}
                                >
                                    {selectedId === station.id && (
                                        <div
                                            style={{
                                                width: 8,
                                                height: 8,
                                                borderRadius: "50%",
                                                background: "#fff"
                                            }}
                                        />
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </Spin>

                <Button
                    type="primary"
                    size="large"
                    block
                    disabled={!selectedId || loading || !!fetchError}
                    onClick={() => onStationSelected(selectedId)}
                    style={{ borderRadius: 8, fontWeight: 600 }}
                >
                    {t("station.bind.confirm")}
                </Button>
            </div>
        </div>
    )
}

export default memo(BindStation)
