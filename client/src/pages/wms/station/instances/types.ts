import type {
    WorkStationEvent,
    WorkStationInfo
} from "@/pages/wms/station/event-loop/types"
import type { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import type { MessageProps } from "@/pages/wms/station/widgets/message"
import type { FunctionComponent, ReactNode, Ref } from "react"

export type ToastFn = (props: MessageProps) => void

export enum DebugType {
    NONE = "none",
    STATIC = "static",
    DYNAMIC = "dynamic"
}

export enum ContainerCarrier {
    conveyor = "CONVEYOR",
    mini_kiva = "MINI_KIVA",
    // large_kiva = "LARGE_KIVA",
    large_kiva = "BUFFER_SHELVING",
    DEFAULT = "DEFAULT",
    kubo = "KUBO",
    shelf = "SHELF"
}

export const PutWallDialogWidth = 1400

export interface DebuggerConfig {
    /** 工作站编码 */
    stationCode?: string
    /** 是否开启调试 */
    debugType?: DebugType
    /** mock数据 */
    mockData?: {}
}

type TitleInfo = (
    workStationInfo: WorkStationInfo,
    workStationEvent: WorkStationEvent<any> | undefined
) => string | number | ReactNode

export interface WorkStationConfig<
    OperationEnum extends string | number | symbol
> extends DebuggerConfig {
    /** 工作站类型 */
    type: WorkStationCommonProps<unknown>["stationOperationType"]
    /** 工作站名称 */
    title: string | ReactNode | TitleInfo
    /** 额外title信息 */
    extraTitleInfo?: string | TitleInfo
    /** 工作站流程描述 */
    stepsDescribe: OperationConfig<OperationEnum>[]
    /** 工作站操作按钮 */
    actions:
        | (TabActionType | Partial<TabAction>)[]
        | ((
              workStationInfo: WorkStationInfo,
              workStationEvent: WorkStationEvent<any> | undefined
          ) => (TabActionType | Partial<TabAction>)[])
    /** 工作站操作配置 */
    operationMap?: Record<OperationEnum, FunctionComponent<any>>
    /** 工作站布局 */
    layout: FunctionComponent<any>
    /* 工作站编码 */
    stationCode?: string
}

export interface OperationConfig<T> {
    /** 操作类型 */
    type: T
    /** 操作名称 */
    name: string | ReactNode
}

export interface CustomActionResponse {
    code: string
    msg: string
    errorCode?: string
    message?: string
}

export interface OperationProps<ExtraData, ConfirmData> {
    /** 当前工作站状态信息 */
    workStationInfo?: WorkStationInfo
    /** 操作组件接收到的value */
    value?: ExtraData
    /** 操作确认接口 */
    onConfirm?: (value: ConfirmData) => Promise<CustomActionResponse>
    /** 自定义动作接口 */
    onCustomActionDispatch: (value: any) => Promise<CustomActionResponse>
    /** 提示语接口 */
    message?: ToastFn
    /** 组件ref */
    refs?: Ref<unknown>
    /** 当前操作是否被激活 */
    isActive?: boolean
}

export enum WorkStationStatus {
    ONLINE = "ONLINE",
    OFFLINE = "OFFLINE"
}

export interface WorkStationCommonProps<T> {
    /** 工作站code */
    stationCode: string
    /** 工作站状态 */
    stationStatus: WorkStationStatus
    /** 工作站类型 */
    stationOperationType: T
    /** 选中区域 */
    chooseArea: string
}

// 按照蓓蕾的图当前kiva上面需要写死一个背篓不能带层级， 所以不能用后端返回的数据， 所以写死一个展示仅做展示
export const defaultData = [
    {
        active: true,
        bay: 1,
        level: 1,
        subContainerBay: "1",
        subContainerLevel: "1",
        enable: true,
        subContainerCode: "",
        subContainerName: ""
    }
]

export const skuInfoMaxLength = 20 // 播种墙展示问题skucodename最多展示20位
