import type { TipsHandlerProps } from "@/pages/wms/station/instances/outbound/operations/tips"
import type { Angle } from "@/pages/wms/station/widgets/common/Container"
import type { ContainerHandlerProps } from "./operations/containerHandler"
import type { PickerArea } from "./operations/pickingHandler"
import type { putWallArea } from "@/pages/wms/station/event-loop/types"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"

export interface OutboundProps {
    [ChooseArea.workLocationArea]: ContainerHandlerProps
    [ChooseArea.skuArea]: PickerArea
    [ChooseArea.putWallArea]: putWallArea
    [ChooseArea.tips]: TipsHandlerProps<any>
}

export interface CarrierDesc {
    /** 总行数 */
    level?: number
    /** 总列数 */
    bay?: number
    /** 设备格口/槽位详情 */
    carrierSlots: CarrierSlot[]
}

export interface CarrierSlot {
    // A42槽口状态
    slotStatus?: string
    // 背景色
    bgColor?: string
    /** 是否正在操作的槽位 */
    active?: boolean
    /** 槽位是否可用 */
    enable: boolean
    /** 设备槽位编码 */
    subContainerCode: string
    /** 槽位上的料箱详情 */
    containerDesc: ContainerDesc
    /** 机器人背篓层数/缓存货架槽位层数 */
    level?: number
    /** 缓存货架槽位列数 */
    bay?: number
    /** 区分是来源箱还是目标箱 */
    businessType?: string
}

export interface ContainerDesc {
    /** 料箱总行数 */
    level: number
    /** 料箱总列数 */
    bay: number
    /** 旋转角度 */
    rotationAngle: Angle
    /** 料箱编码 */
    containerCode: string
    /** 料箱规格 */
    containerType: string
    /** 面 */
    face?: string
    /** 格口详情 */
    subContainers: SubContainer[]
    /** 是否当前操作项 */
    active?: boolean
    height?: number
    width?: number | string
}

export interface SubContainer {
    slotStatus?: any

    /** 第几行 */
    level: number
    /** 第几列 */
    bay?: number
    /** 格口是否可用 */
    enable?: boolean
    /** 是否正在操作的格口 */
    active?: boolean
    /** 格口编码 */
    subContainerCode?: string
    /** 格口名称 */
    subContainerName?: string
    containerCode?: string
    stationSlot?: string
    subContainerBay: string
    subContainerLevel: string
    stationSlotStatus?: string
    subContainerStatus?: string
    /** 区分是来源箱还是目标箱 */
    businessType?: string
}

export enum PutWallSlotStatus {
    /** 空闲 */
    IDLE = "IDLE",
    /** 待绑定 */
    WAITING_BINDING = "WAITING_BINDING",
    /** 已绑定 */
    BOUND = "BOUND",
    /** 待分拨 */
    DISPATCH = "DISPATCH",
    // 拣货挂单拆箱选中状态（不在标准状态之中）
    _SELECTED = "_SELECTED",
    /** 待封箱 */
    WAITING_SEAL = "WAITING_SEAL",
    /** 待挂起 */
    WAITING_HANG = "WAITING_HANG"
}

export enum BreathingLamp {
    /** 封箱呼吸灯模式 */
    WAITING_SEAL = "seeding-flash",
    /** 分播呼吸灯模式 */
    DISPATCH = "seeding-dispatch-flash",
    /** 挂单呼吸灯模式 */
    WAITING_HANG = "seeding-waiting-hang-flash"
}
