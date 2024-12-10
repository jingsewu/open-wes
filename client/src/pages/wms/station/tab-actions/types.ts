import type { ModalProps } from "antd"
import type { FunctionComponent, ReactNode, RefObject } from "react"
import type { RouteChildrenProps } from "react-router"

import type {
    SendEventPayload,
    WorkStationEvent,
    WorkStationInfo
} from "@/pages/wms/station/event-loop/types"
import type {
    CustomActionResponse,
    ToastFn
} from "@/pages/wms/station/instances/types"

export enum TabActionModalType {
    /** 常规模态框 */
    NORMAL,
    /** 全屏模态框 */
    FULL_SCREEN,
    /** 无模态框 */
    NONE,
    /** 确认模态框 */
    CONFIRM
}

export interface EmitterPayload {
    /** 自定义事件触发方法 */
    onCustomActionDispatch: (
        action: SendEventPayload
    ) => Promise<CustomActionResponse>
    /** 各个Operation中的ref对象 */
    operationsMap: Record<string, any>
    /** 重置模态框方法 */
    resetModal?: () => void
    /** 控制路由跳转方法 */
    history: any
    /** toast提示方法 */
    message: ToastFn
    /** 模态框Ref */
    refs?: RefObject<any>
    /** 设置模态框状态 */
    setModalVisible: (visible: boolean) => void
    /** 设置确定按钮loading状态 */
    setConfirmLoading?: (visible: boolean) => void
    /** 确定按钮loading状态 */
    loading?: boolean
    /** 工作站信息 */
    workStationEvent?: WorkStationEvent<any> | undefined
    workStationInfo?: WorkStationInfo | undefined
}

export interface TabAction {
    /** 底部按钮的key */
    key: string
    /** 底部按钮展示名称 */
    name: string | ReactNode
    /** 底部按钮展示icon */
    icon?: any
    permissions?: number[]
    position?: "left" | "right"
    /** 点击按钮后弹出模态框的类型 */
    modalType: TabActionModalType
    /** 弹出模态框配置 */
    modalConfig?: Omit<Partial<ModalProps>, "visible">
    /** 点击弹出模态框中的内容 modalType为NONE是不需要传递 */
    content?: FunctionComponent<EmitterPayload>
    /** 点击后需要变换成的tab */
    trigger?: Pick<TabAction, "name" | "icon" | "emitter">
    /** 点击按钮后需要触发的请求，若modalType非NONE则会在modal框确认时触发 */
    emitter: (payload: EmitterPayload) => Promise<void>
    /** 模态框确认按钮回调 */
    onSubmit?: (ref: RefObject<any>, props: EmitterPayload) => Promise<boolean>
    /** 是否禁用 */
    disabled?:
        | boolean
        | ((
              payload: WorkStationEvent<any> | undefined,
              info?: WorkStationInfo
          ) => boolean)
    /** 是否隐藏 */
    hide?: boolean | ((payload: WorkStationEvent<any> | undefined) => boolean)
    testid?: string
}

/**
 * @description: 工作站业务类型
 */
export enum BusinessType {
    /** 入库 */
    INBOUND = "INBOUND",
    /** 出库 */
    OUTBOUND = "OUTBOUND",
    /** 收货 */
    RECEIVE = "RECEIVE",
    /** 退货分理 */
    DIVIDE = "DIVIDE",
    /** 智能选箱下架 */
    REPLENISH = "REPLENISH",
    /** 随机上架 */
    RANDOM_REPLENISH = "RANDOM_REPLENISH",
    /** 空箱出库 */
    EMPTY_CONTAINER_OUTBOUND = "EMPTY_CONTAINER_OUTBOUND",
    /** 盘点 */
    STOCK_TAKE = "STOCK_TAKE",
    /** 一步式理库 */
    ONE_STEP_INVENTORY_RELOCATION = "ONE_STEP_INVENTORY_RELOCATION",
    /** 两步式理库 */
    TWO_STEP_INVENTORY_RELOCATION = "TWO_STEP_INVENTORY_RELOCATION",
    /** 养护 */
    MAINTAIN = "MAINTAIN",
    /** 理库 */
    TOTE_RELOCATION = "TOTE_RELOCATION",
    /** 人工选箱上架 */
    ARTIFICIAL_REPLENISH = "ARTIFICIAL_REPLENISH"
}

/**
 * @description: 工作站任务类型
 */
export enum TaskType {
    /** 入库 */
    INBOUND = "INBOUND",
    /** 出库 */
    OUTBOUND = "OUTBOUND"
}
