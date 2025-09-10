import type {WorkStationView} from "../event-loop/types"
import type {TabActionType} from "../tab-actions/constant"
import type {TabAction} from "../tab-actions/types"
import type {MessageProps} from "../widgets/message"
import type {FunctionComponent, ReactNode, Ref} from "react"

export type ToastFn = (props: MessageProps) => void

export enum DebugType {
    NONE = "none",
    STATIC = "static",
    DYNAMIC = "dynamic"
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
    workStationEvent: WorkStationView<any> | undefined
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
              workStationEvent: WorkStationView<any> | undefined
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
    /** 操作组件接收到的value */
    value?: ExtraData
    /** 自定义动作接口 */
    onActionDispatch: (value: any) => Promise<CustomActionResponse>
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
