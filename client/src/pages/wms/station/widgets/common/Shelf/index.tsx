import type { CustomActionResponse } from "@/pages/wms/station/instances/types"
import ShelfModel from "./ShelfModel"
import Code from "./Code"
import React from "react"
import { useTranslation } from "react-i18next"
import { Typography } from "antd"
const { Title } = Typography

export interface MaterialProps {
    onCustomActionDispatch?: (value: any) => void
    arrivedContainer: subContainerInterface
    allowContainer?: boolean
    showSlotCode?: boolean
    showAllSlots?: boolean
}
interface subContainerInterface {
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

export interface MaterialConfirmProps {
    skuCode: string
}
const MaterialRack = (props: MaterialProps) => {
    const { t } = useTranslation()
    const {
        arrivedContainer,
        showAllSlots,
        showSlotCode,
        onCustomActionDispatch
    } = props
    const {
        activeSlotCodes = [],
        containerCode,
        containerSpec,
        disabledSlotCodes = [],
        recommendSlotCodes = []
    } = arrivedContainer || {}
    const toBePickdSlot = containerSpec?.containerSlotSpecs.find((item) =>
        activeSlotCodes?.includes(item.containerSlotSpecCode)
    )
    return (
        <div className="d-flex flex-col items-center w-full h-full">
            {/* <Code
                containerCode={containerCode}
                face={arrivedContainer?.face}
                level={toBePickdSlot?.level}
                bay={toBePickdSlot?.locBay}
            /> */}
            <Title level={4}>{containerCode}</Title>

            <ShelfModel
                containerSlotSpecs={containerSpec?.containerSlotSpecs}
                activeSlotCodes={activeSlotCodes}
                disabledSlotCodes={disabledSlotCodes}
                recommendSlotCodes={recommendSlotCodes}
                showAllSlots={showAllSlots}
                onCustomActionDispatch={onCustomActionDispatch}
            />
        </div>
    )
}

// export const valueFilter = (
//     data: WorkStationEvent<any>
// ): OperationProps<MaterialProps, MaterialConfirmProps>["value"] => {
//     return pick(data, ["skuInfo"])
// }
export default MaterialRack
