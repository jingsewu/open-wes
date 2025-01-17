import React from "react"
import { Typography } from "antd"

import EmptyImg from "@/icon/default_empty_image.png"

import ScanContainer from "./ScanContainer"
import {Translation} from "react-i18next";

const { Title } = Typography
const outboundStatusText = {
    NO_TASK: <Translation>{(t) => t("station.NO_TASK")}</Translation>,
    WAIT_ROBOT: <Translation>{(t) => t("station.WAITING_ROBOT")}</Translation>,
    DO_OPERATION: null
}

const EmptyImage = ({ workStationInfo, onChange, handleShowInput }: any) => {
    const { devicePhysicalTypeList = [], extendsRunningInfo = {} } =
        workStationInfo
    // 多子站工作站模式，如果子站包含输送线，默认页需显示扫料箱码按钮
    const isIncludeConveyorStation =
        devicePhysicalTypeList.length > 1 &&
        devicePhysicalTypeList.includes("CONVEYOR")
    // 是否开启多拣选位
    const conveyorMultiplePickingPositions =
        workStationInfo?.extendsRunningInfo?.outboundCommonConfig
            ?.conveyorMultiplePickingPositions === "TRUE"
    return (
        <>
            <img src={EmptyImg} alt="" style={{ height: 200 }} />
            <Title level={4}>
                {
                    outboundStatusText[
                        extendsRunningInfo.outboundWorkStatus as keyof typeof outboundStatusText
                    ]
                }
            </Title>
            {isIncludeConveyorStation ? (
                <ScanContainer
                    conveyorMultiplePickingPositions={
                        conveyorMultiplePickingPositions
                    }
                    handleShowInput={handleShowInput}
                    onChange={onChange}
                    isDefaultPage={true}
                />
            ) : null}
        </>
    )
}

export default EmptyImage
