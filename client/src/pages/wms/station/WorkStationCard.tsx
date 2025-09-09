import { Col, Row } from "antd"
import React, { useContext, useEffect } from "react"
import { Translation } from "react-i18next"
import { withRouter } from "react-router-dom"
import * as images from "@/icon/station"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { WorkStationContext } from "@/pages/wms/station/event-loop/provider"
import StationCard from "@/pages/wms/station/widgets/StationCard"

export const WORK_STATION_PATH_PREFIX = "/wms/workStation"

export enum StationTypes {
    RECEIVE = "receive",

    SELECT_CONTAINER_PUT_AWAY = `select_container_put_away`,

    PICKING = "outbound",

    STOCKTAKE = "stocktake",
}

export enum WorkflowCategory {
    RECEIVING = "RECEIVING",
    PUT_AWAY = "PUT_AWAY",
    PICKING = "PICKING",
    STOCKTAKE = "STOCKTAKE"
}

interface CardOption {
    title: React.ReactNode;
    value: string;
    category: WorkflowCategory;
    description: React.ReactNode;
    avatar: any;
    rightIcon: any;
    backgroundColor?: string;
    hasOrder?: boolean;
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
        title: <Translation>{(t) => t("receiving.unplanned.title")}</Translation>,
        value: "RECEIVE",
        hasOrder: false,
        category: WorkflowCategory.RECEIVING,
        description: (
            <Translation>{(t) => t("receiving.unplanned.cardDescription")}</Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#f6ffed"
    },

    // PUT AWAY Operations
    {
        title: <Translation>{(t) => t("select_container_put_away.station.title")}</Translation>,
        value: "SELECT_CONTAINER_PUT_AWAY",
        category: WorkflowCategory.PUT_AWAY,
        hasOrder: true,
        description: (
            <Translation>{(t) => t("select_container_put_away.station.cardDescription")}</Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#fff2e8"
    },
    {
        title: <Translation>{(t) => t("no_order_select_container_put_away.station.title")}</Translation>,
        value: "SELECT_CONTAINER_PUT_AWAY",
        category: WorkflowCategory.PUT_AWAY,
        hasOrder: false,
        description: (
            <Translation>{(t) => t("no_order_select_container_put_away.station.cardDescription")}</Translation>
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

// Group cards by category
const groupedCards = cardOptions.reduce((acc, card) => {
    if (!acc[card.category]) {
        acc[card.category] = []
    }
    acc[card.category].push(card)
    return acc
}, {} as Record<WorkflowCategory, CardOption[]>)

const Station = (props: any) => {
    const { history, workStationEvent } = props
    const { workStationStatus, workStationMode } = workStationEvent || {}
    const { onActionDispatch } = useContext(WorkStationContext)

    useEffect(() => {
        const path =
            workStationStatus !== "OFFLINE" && workStationMode
                ? `${WORK_STATION_PATH_PREFIX}/${
                    StationTypes[workStationMode as keyof typeof StationTypes]
                }`
                : WORK_STATION_PATH_PREFIX
        history.replace(path)
    }, [workStationMode, workStationStatus])

    const handleCardClick = (workStationMode: string, hasOrder: boolean) => {
        onActionDispatch({
            eventCode: CustomActionType.ONLINE,
            data: {
                "workStationMode": workStationMode,
                "hasOrder": hasOrder
            }
        })
    }

    const renderCategorySection = (category: WorkflowCategory, cards: CardOption[]) => (
        <div key={category} className="mb-6">
            <h3 className="text-lg font-semibold mb-3 capitalize">
                <Translation>{(t) => t(`categories.${category.toLowerCase()}`)}</Translation>
            </h3>
            <Row gutter={[24, {xs: 8, sm: 16, md: 24}]}>
                {cards.map((item) => (
                    <Col md={24} lg={12} key={item.value}>
                        <StationCard
                            {...item}
                            handleCardClick={() => handleCardClick(item.value, !!item.hasOrder)}
                        />
                    </Col>
                ))}
            </Row>
        </div>
    )

    return (
        <div className="site-card-wrapper px-4 pt-4">
            {Object.entries(groupedCards).map(([category, cards]) =>
                renderCategorySection(category as WorkflowCategory, cards)
            )}
        </div>
    )
}

export default withRouter(Station)
