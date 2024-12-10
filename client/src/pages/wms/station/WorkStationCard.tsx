import { Col, Row } from "antd"
import React, { useEffect, useContext } from "react"
import { Translation } from "react-i18next"
import request from "@/utils/requestInterceptor"

import { withRouter } from "react-router-dom"
import * as images from "@/icon/station"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { APIContext } from "@/pages/wms/station/event-loop/provider"
import StationCard from "@/pages/wms/station/widgets/StationCard"

export const WORK_STATION_PATH_PREFIX = "/wms/workStation"

export enum StationTypes {
    // RECOMMEND_REPLENISH = `outbound`,
    SELECT_CONTAINER_PUT_AWAY = `replenish`,
    WITHOUT_ORDER_PUT_AWAY = `without_order_replenish`,
    PICKING = `outbound`,
    STOCKTAKE = `stocktake`,
    ONE_STEP_RELOCATION = `oneStepInventory`,
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
    // {
    //     title: <Translation>{(t) => t("replenish.title")}</Translation>,
    //     value: "SELECT_CONTAINER_PUT_AWAY",
    //     description: (
    //         <Translation>{(t) => t("replenish.cardDescription")}</Translation>
    //     ),
    //     avatar: images.xzrqsj,
    //     rightIcon: images.xzrqsjbg,
    //     backgroundColor: "#f8f3ff"
    // },
    // {
    //     title: <Translation>{(t) => t("noOrdersReplenish.title")}</Translation>,
    //     value: "WITHOUT_ORDER_PUT_AWAY",
    //     description: (
    //         <Translation>
    //             {(t) => t("noOrdersReplenish.cardDescription")}
    //         </Translation>
    //     ),
    //     avatar: images.zxsj,
    //     rightIcon: images.zxsjbg,
    //     backgroundColor: "#f2f5ff"
    // },
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
    // {
    //     title: <Translation>{(t) => t("library.title")}</Translation>,
    //     value: "ONE_STEP_RELOCATION",
    //     description: (
    //         <Translation>{(t) => t("library.cardDescription")}</Translation>
    //     ),
    //     avatar: images.lk,
    //     rightIcon: images.lkbg
    // }
    // {
    //     title: <Translation>{(t) => t("library.title")}</Translation>,
    //     value: "ONE_STEP_RELOCATION",
    //     description: (
    //         <Translation>{(t) => t("library.cardDescription")}</Translation>
    //     ),
    //     avatar: images.lk,
    //     rightIcon: images.lkbg
    // }
]

const Station = (props: any) => {
    const { history, location, workStationEvent } = props
    const { workStationStatus, operationType, workStationMode } =
        workStationEvent || {}
    const { onCustomActionDispatch } = useContext(APIContext)

    // useEffect(() => {
    //     const path = `${WORK_STATION_PATH_PREFIX}/inventory`
    //     history.replace(path)
    // }, [])

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
        onCustomActionDispatch({
            eventCode: CustomActionType.ONLINE,
            data
        })
    }
    return (
        <div className="site-card-wrapper px-4 pt-4">
            <Row gutter={[24, { xs: 8, sm: 16, md: 24 }]}>
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
