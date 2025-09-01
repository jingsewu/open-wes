import {Col, Row} from "antd"
import React, {useContext, useEffect} from "react"
import {Translation} from "react-i18next"

import {withRouter} from "react-router-dom"
import * as images from "@/icon/station"
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType"
import {APIContext} from "@/pages/wms/station/event-loop/provider"
import StationCard from "@/pages/wms/station/widgets/StationCard"

export const WORK_STATION_PATH_PREFIX = "/wms/workStation"

export enum StationTypes {
    SELECT_CONTAINER_PUT_AWAY = `select_container_put_away`,
    WITHOUT_ORDER_PUT_AWAY = `without_order_replenish`,
    PICKING = `outbound`,
    STOCKTAKE = `stocktake`,
    RECEIVE = "receive"
}

const cardOptions = [
    {
        title: <Translation>{(t) => t("receiving.title")}</Translation>,
        value: "RECEIVE",
        description: (
            <Translation>{(t) => t("receiving.cardDescription")}</Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#f8f3ff"
    },
    {
        title: <Translation>{(t) => t("select_container_put_away.title")}</Translation>,
        value: "SELECT_CONTAINER_PUT_AWAY",
        description: (
            <Translation>{(t) => t("select_container_put_away.cardDescription")}</Translation>
        ),
        avatar: images.spsh,
        rightIcon: images.spshbg,
        backgroundColor: "#f8f3ff"
    },
    {
        title: <Translation>{(t) => t("picking.title")}</Translation>,
        value: "PICKING",
        description: (
            <Translation>{(t) => t("picking.cardDescription")}</Translation>
        ),
        avatar: images.jh,
        rightIcon: images.jhbg
    },
    {
        title: <Translation>{(t) => t("inventory.title")}</Translation>,
        value: "STOCKTAKE",
        description: (
            <Translation>{(t) => t("inventory.cardDescription")}</Translation>
        ),
        avatar: images.pd,
        rightIcon: images.pdbg
    },
]

const Station = (props: any) => {
    const {history, workStationEvent} = props
    const {workStationStatus, workStationMode} =
    workStationEvent || {}
    const {onActionDispatch} = useContext(APIContext)

    useEffect(() => {
        const path =
            workStationStatus !== "OFFLINE" && workStationMode
                ? `${WORK_STATION_PATH_PREFIX}/${
                    StationTypes[workStationMode as keyof typeof StationTypes]
                }`
                : WORK_STATION_PATH_PREFIX
        history.replace(path)
    }, [workStationMode, workStationStatus])

    const handleCardClick = (data: string) => {
        onActionDispatch({
            eventCode: CustomActionType.ONLINE,
            data
        })
    }
    return (
        <div className="site-card-wrapper px-4 pt-4">
            <Row gutter={[24, {xs: 8, sm: 16, md: 24}]}>
                {cardOptions.map((item) => {
                    return (
                        <Col md={24} lg={12} key={item.value}>
                            <StationCard
                                {...item}
                                handleCardClick={() =>
                                    handleCardClick(item.value)
                                }
                            />
                        </Col>
                    )
                })}
            </Row>
        </div>
    )
}

export default withRouter(Station)
