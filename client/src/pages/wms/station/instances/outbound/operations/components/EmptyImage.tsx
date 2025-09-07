import React from "react"
import {Typography} from "antd"

import EmptyImg from "@/icon/default_empty_image.png"

import ScanContainer from "./ScanContainer"
import {WorkLocationViews} from "@/pages/wms/station/event-loop/types";

const {Title} = Typography

const EmptyImage = ({workStationEvent, onChange, handleShowInput}: any) => {
    const {workLocationArea} = workStationEvent
    const isIncludeConveyorStation = workLocationArea?.workLocationViews?.length > 1
        && workLocationArea?.workLocationViews?.find((item: WorkLocationViews) => item.enable).includes("CONVEYOR")
    return (
        <>
            <img src={EmptyImg} alt="" style={{height: 200}}/>
            <Title level={4}>
            </Title>
            {isIncludeConveyorStation ? (
                <ScanContainer
                    handleShowInput={handleShowInput}
                    onChange={onChange}
                    isDefaultPage={true}
                />
            ) : null}
        </>
    )
}

export default EmptyImage
