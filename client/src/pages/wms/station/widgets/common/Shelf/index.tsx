import ShelfModel from "./ShelfModel"
import React from "react"
import {Typography} from "antd"

const {Title} = Typography

export interface MaterialProps {
    onActionDispatch?: (value: any) => void
    arrivedContainer: SubContainerInterface
    allowContainer?: boolean
    showSlotCode?: boolean
    showAllSlots?: boolean
}

interface SubContainerInterface {
    containerSlotSpecCode?: string
    face?: string
    level?: number
    bay?: number
    containerCode: string
    activeSlotCodes?: string[]
    containerSpec: ContainerSpec
    disabledSlotCodes?: string[]
    recommendSlotCodes?: string[]
}

interface ContainerSpec {
    containerSlotSpecs: SubContainerListFace[]
}

interface SubContainerListFace {
    bay: string
    level: string
    containerSlotSpecCode: string
    locLevel: number
    locBay: number
}

const MaterialRack = (props: MaterialProps) => {
    const {
        arrivedContainer,
        showAllSlots,
        onActionDispatch
    } = props
    const {
        activeSlotCodes = [],
        containerCode,
        containerSpec,
        disabledSlotCodes = [],
        recommendSlotCodes = []
    } = arrivedContainer || {}
    return (
        <div className="d-flex flex-col items-center w-full h-full">
            <Title level={4}>{containerCode}</Title>

            <ShelfModel
                containerSlotSpecs={containerSpec?.containerSlotSpecs}
                activeSlotCodes={activeSlotCodes}
                disabledSlotCodes={disabledSlotCodes}
                recommendSlotCodes={recommendSlotCodes}
                showAllSlots={showAllSlots}
                onActionDispatch={onActionDispatch}
            />
        </div>
    )
}

export default MaterialRack
