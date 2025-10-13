/**
 * 出库工作站类型定义
 */
import type { TipsHandlerProps } from "../operations/tips"
import type { ContainerHandlerProps } from "../operations/containerHandler"
import type { PickerArea } from "../operations/pickingHandler"
import type { PutWallArea } from "@/pages/wms/station/event-loop/types"
import type { Angle } from "@/pages/wms/station/widgets/common/Container"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"

// 出库工作站属性接口
export interface OutboundProps {
    [ChooseArea.workLocationArea]: ContainerHandlerProps
    [ChooseArea.skuArea]: PickerArea
    [ChooseArea.putWallArea]: PutWallArea
    [ChooseArea.tips]: TipsHandlerProps<any>
}

// 容器相关类型
export interface SubContainer {
    slotStatus?: any
    level: number
    bay?: number
    enable?: boolean
    active?: boolean
    subContainerCode?: string
    subContainerName?: string
    containerCode?: string
    stationSlot?: string
    subContainerBay: string
    subContainerLevel: string
    stationSlotStatus?: string
    subContainerStatus?: string
    businessType?: string
}

export interface ContainerDesc {
    level: number
    bay: number
    rotationAngle: Angle
    containerCode: string
    containerType: string
    face?: string
    subContainers: SubContainer[]
    active?: boolean
    height?: number
    width?: number | string
}

export interface CarrierSlot {
    slotStatus?: string
    bgColor?: string
    active?: boolean
    enable: boolean
    subContainerCode: string
    containerDesc: ContainerDesc
    level?: number
    bay?: number
    businessType?: string
}

export interface CarrierDesc {
    level?: number
    bay?: number
    carrierSlots: CarrierSlot[]
}

// 播种墙相关类型
export enum PutWallSlotStatus {
    IDLE = "IDLE",
    WAITING_BINDING = "WAITING_BINDING",
    BOUND = "BOUND",
    DISPATCH = "DISPATCH",
    _SELECTED = "_SELECTED",
    WAITING_SEAL = "WAITING_SEAL",
    WAITING_HANG = "WAITING_HANG"
}

export enum BreathingLamp {
    WAITING_SEAL = "seeding-flash",
    DISPATCH = "seeding-dispatch-flash",
    WAITING_HANG = "seeding-waiting-hang-flash"
}
