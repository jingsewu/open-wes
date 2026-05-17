import {Col, Row} from "antd"
import React, {useEffect, useRef, useMemo, useCallback, useState} from "react"
import {useTranslation} from "react-i18next"
import {Translation} from "react-i18next"
import {withRouter} from "react-router-dom"
import * as images from "@/icon/station"
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType"
import {useWorkStation, observer} from "@/pages/wms/station/state"
import StationCard from "@/pages/wms/station/widgets/StationCard"

export const WORK_STATION_PATH_PREFIX = "/wms/workStation"

export enum StationTypes {
    RECEIVE = "receive",

    SELECT_CONTAINER_PUT_AWAY = `select_container_put_away`,

    PICKING = "outbound",

    STOCKTAKE = "stocktake"
}

export enum WorkflowCategory {
    RECEIVING = "RECEIVING",
    PUT_AWAY = "PUT_AWAY",
    PICKING = "PICKING",
    STOCKTAKE = "STOCKTAKE"
}

interface CardOption {
    title: React.ReactNode
    value: string
    category: WorkflowCategory
    description: React.ReactNode
    avatar: any
    rightIcon: any
    backgroundColor?: string
    hasOrder?: boolean
}

const cardOptions: CardOption[] = [
    // RECEIVING Operations
    {
        title: <Translation>{(t) => t("receiving.title")}</Translation>,
        value: "RECEIVE",
        category: WorkflowCategory.RECEIVING,
        hasOrder: true,
        description: (
            <Translation>{(t) => t("receiving.cardDescription")}</Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#e6f7ff"
    },
    {
        title: (
            <Translation>{(t) => t("receiving.unplanned.title")}</Translation>
        ),
        value: "RECEIVE",
        hasOrder: false,
        category: WorkflowCategory.RECEIVING,
        description: (
            <Translation>
                {(t) => t("receiving.unplanned.cardDescription")}
            </Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#f6ffed"
    },

    // PUT AWAY Operations
    {
        title: (
            <Translation>
                {(t) => t("select_container_put_away.station.title")}
            </Translation>
        ),
        value: "SELECT_CONTAINER_PUT_AWAY",
        category: WorkflowCategory.PUT_AWAY,
        hasOrder: true,
        description: (
            <Translation>
                {(t) => t("select_container_put_away.station.cardDescription")}
            </Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#fff2e8"
    },
    {
        title: (
            <Translation>
                {(t) => t("no_order_select_container_put_away.station.title")}
            </Translation>
        ),
        value: "SELECT_CONTAINER_PUT_AWAY",
        category: WorkflowCategory.PUT_AWAY,
        hasOrder: false,
        description: (
            <Translation>
                {(t) =>
                    t(
                        "no_order_select_container_put_away.station.cardDescription"
                    )
                }
            </Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#fff2e8"
    },

    // PICKING Operations
    {
        title: <Translation>{(t) => t("picking.title")}</Translation>,
        value: "PICKING",
        category: WorkflowCategory.PICKING,
        description: (
            <Translation>{(t) => t("picking.cardDescription")}</Translation>
        ),
        avatar: images.jh,
        rightIcon: images.jhbg,
        backgroundColor: "#f6ffed"
    },

    // STOCKTAKE Operations
    {
        title: <Translation>{(t) => t("inventory.title")}</Translation>,
        value: "STOCKTAKE",
        category: WorkflowCategory.STOCKTAKE,
        description: (
            <Translation>{(t) => t("inventory.cardDescription")}</Translation>
        ),
        avatar: images.pd,
        rightIcon: images.pdbg,
        backgroundColor: "#fff2e8"
    }
]

const groupedCards = cardOptions.reduce((acc, card) => {
    if (!acc[card.category]) {
        acc[card.category] = []
    }
    acc[card.category].push(card)
    return acc
}, {} as Record<WorkflowCategory, CardOption[]>)

const Station = observer((props: any) => {
    const {history, clearStation} = props
    const {t} = useTranslation()
    const {store, onActionDispatch} = useWorkStation()
    const {workStationEvent} = store
    const [bannerDismissed, setBannerDismissed] = useState(false)

    const {workStationStatus, workStationMode} = workStationEvent || {}
    const boundStationCode = workStationEvent?.stationCode || localStorage.getItem("stationId")
    const showUnbindBanner = !bannerDismissed && !!localStorage.getItem("stationId")

    useEffect(() => {
        // Do not navigate while store is empty (e.g., immediately after
        // eventLoop.stop() resets the store on exit).
        if (!workStationEvent) return

        const targetPath =
            workStationStatus !== "OFFLINE" && workStationMode
                ? `${WORK_STATION_PATH_PREFIX}/${
                      StationTypes[workStationMode as keyof typeof StationTypes]
                  }`
                : WORK_STATION_PATH_PREFIX

        if (history.location.pathname !== targetPath) {
            history.replace(targetPath)
        }
    }, [workStationEvent, workStationMode, workStationStatus, history])

    const handleCardClick = useCallback((workStationMode: string, hasOrder: boolean) => {
        onActionDispatch({
            eventCode: CustomActionType.ONLINE,
            data: {
                workStationMode: workStationMode,
                hasOrder: hasOrder
            }
        })
    }, [onActionDispatch])

    const handleUnbind = () => {
        if (clearStation) clearStation()
    }

    return (
        <div className="site-card-wrapper" style={{ backgroundColor: "#fff", minHeight: "100%" }}>
            {showUnbindBanner && (
                <div
                    style={{
                        background: "#fff7ed",
                        borderBottom: "1px solid #fed7aa",
                        padding: "10px 20px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        gap: 12
                    }}
                >
                    <div style={{display: "flex", alignItems: "center", gap: 8}}>
                        <span style={{fontSize: 16}}>🔔</span>
                        <div>
                            <span style={{fontSize: 13, fontWeight: 600, color: "#92400e"}}>
                                {t("station.bind.boundNotice", {code: boundStationCode})}
                            </span>
                            <span style={{fontSize: 12, color: "#b45309", marginLeft: 8}}>
                                {t("station.bind.autoConnect")}
                            </span>
                        </div>
                    </div>
                    <div style={{display: "flex", gap: 8, flexShrink: 0}}>
                        <button
                            onClick={() => setBannerDismissed(true)}
                            style={{
                                padding: "4px 12px",
                                background: "#fff",
                                border: "1px solid #d97706",
                                borderRadius: 5,
                                fontSize: 12,
                                color: "#b45309",
                                cursor: "pointer"
                            }}
                        >
                            {t("station.bind.keepBinding")}
                        </button>
                        <button
                            onClick={handleUnbind}
                            style={{
                                padding: "4px 12px",
                                background: "#d97706",
                                border: "none",
                                borderRadius: 5,
                                fontSize: 12,
                                color: "#fff",
                                cursor: "pointer",
                                fontWeight: 600
                            }}
                        >
                            {t("station.bind.unbind")}
                        </button>
                    </div>
                </div>
            )}
        <div className="px-4 pt-4">
            {Object.entries(groupedCards).map(([category, cards]) => (
                <div key={category} className="mb-6">
                    <h3 className="text-lg font-semibold mb-3 capitalize">
                        <Translation>
                            {(t) => t(`categories.${category.toLowerCase()}`)}
                        </Translation>
                    </h3>
                    <Row gutter={[24, {xs: 8, sm: 16, md: 24}]}>
                        {cards.map((item) => (
                            <Col md={24} lg={12} key={item.value + item.hasOrder}>
                                <StationCard
                                    {...item}
                                    handleCardClick={() =>
                                        handleCardClick(
                                            item.value,
                                            !!item.hasOrder
                                        )
                                    }
                                />
                            </Col>
                        ))}
                    </Row>
                </div>
            ))}
        </div>
        </div>
    )
})

export default withRouter(Station)
