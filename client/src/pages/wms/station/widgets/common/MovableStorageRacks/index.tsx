import React from "react"
import Racks from "./Racks"
import LocationInfo from "./LocationInfo"
import { Descriptions } from "antd"

const Combo = (props: any) => {
    const {
        arrivedContainer,
        showAllSlots = true,
        onCustomActionDispatch,
        backgroundColor
    } = props

    const {
        activeSlotCodes = [],
        containerCode,
        containerId,
        face,
        containerSpec,
        disabledSlotCodes = [],
        recommendSlotCodes = [],
        containerAttributes = {}
    } = arrivedContainer || {}

    return (
        <>
            <Racks
                {...arrivedContainer}
                showAllSlots={showAllSlots}
                onCustomActionDispatch={onCustomActionDispatch}
                backgroundColor={backgroundColor}
            />
            <LocationInfo
                containerCode={containerCode}
                containerId={containerId}
                face={face}
                containerAttributes={containerAttributes}
                onCustomActionDispatch={onCustomActionDispatch}
            />
        </>
    )
}

export default Combo
