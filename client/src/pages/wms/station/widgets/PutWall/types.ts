import type { ReactNode } from "react"
import type {
    PutWallDisplayStyle,
    putWallViewsItem,
    putWallSlotsItem,
    Location,
    PutWallTagConfigDTO
} from "@/pages/wms/station/event-loop/types"

export interface PutWallProps {
    putWallDisplayStyle?: PutWallDisplayStyle
    putWallViews?: putWallViewsItem[]
    putWallTagConfigDTO?: PutWallTagConfigDTO
    putWallStatusTextMap?: { [key: string]: any }
    isActive?: boolean
    onPressEnter?: (value: string) => Promise<void>
    onSlotClick?: (item: putWallSlotsItem) => void
    /** 格口最大显示字段数量 用于定制写死 */
    maxSlotFieldsCount?: number
    /** 面切换事件 */
    onLocationChange?: (location: Location) => void
}

export enum PutWallColorEnum {
    /** 默认颜色 */
    DEFAULT = "default",
    /** 可选可用色 */
    TOBESELECT = "toBeSelect",
    /** 选中色 */
    SELECTED = "selected",
    /** 禁用色 */
    DISABLED = "disabled",
    /** 异常色 */
    EXCEPTION = "exception"
}

export enum SlotColor {
    RED = "#f87171",
    BLUE = "#60a5fa",
    GREEN = "#34d399",
    GRAY = "#d1d5db"
}

// export enum PutWallDisplayStyle {
//     /** 播种墙合并显示 */
//     Merge = "MERGE",
//     /** 播种墙tab显示 */
//     Split = "SPLIT"
// }

export interface SlotData {
    /** 行 */
    level: string
    /** 列 */
    bay: string
    locLevel: number
    locBay: number
    status: PutWallStatus
    /** 格口是否可用 */
    enable: boolean
    subContainerCode: string
    /** 格口文案配置list */
    putWallSlotDesc: PutWallSlotDesc[]
    putWallSlotStatus: PutWallStatus
    face: string
    putWallSlotCode: string
}

export type PutWallStatusMap = {
    [key in PutWallStatus as string]: {
        /** 颜色配置项 */
        color?: PutWallColor
        /** 图例文案 有则显示在图例上 */
        text?: ReactNode
        /** 呼吸灯 className */
        breathingLamp?: string
    }
}

export enum BreathingLampClassName {
    /** 封箱呼吸灯模式 */
    WAITING_SEAL = "seeding-flash",
    /** 分播呼吸灯模式 */
    DISPATCH = "seeding-dispatch-flash",
    /** 挂单呼吸灯模式 */
    WAITING_HANG = "seeding-waiting-hang-flash"
}

export interface PutWallSlotDesc {
    textSize: number
    bold: string
    color: any
    propertyName: string
    propertyValue: string | number
}

export enum PutWallStatus {
    /** 空闲 */
    IDLE = "IDLE",
    /** 已绑定 */
    BOUND = "BOUND",
    /** 待绑定 */
    WAITING_BINDING = "WAITING_BINDING",
    /** 待封箱 */
    WAITING_SEAL = "WAITING_SEAL",
    /** 待分拨 */
    DISPATCH = "DISPATCH",
    /** 待挂起 拆箱用 */
    // WAITING_HANG = "WAITING_HANG",
    /** 禁用 处理enable: ture => DISABLE */
    OPTIONAL = "OPTIONAL",
    SELECTED = "SELECTED",
    DISABLE = "DISABLE",

    /** 拣货挂单拆箱选中状态（不在标准状态之中） */
    _SELECTED = "_SELECTED"
}

interface PutWallColor {
    backgroundColor: string
    borderColor: string
    textColor: string
}
