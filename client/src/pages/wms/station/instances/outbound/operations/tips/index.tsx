import type { FC, RefObject } from "react"
import React from "react"
import { useTranslation } from "react-i18next"

import { useWorkStation } from "@/pages/wms/station/state"
import ExceptionLog from "@/pages/wms/station/instances/outbound/operations/tips/Abnormal"
import CloseContainer from "@/pages/wms/station/instances/outbound/operations/tips/close-container"
import EmptyContainerHandler from "@/pages/wms/station/instances/outbound/operations/tips/empty-container-handler"
import MessageRemind from "@/pages/wms/station/instances/outbound/operations/tips/message-remind"
import ScanErrorRemind from "@/pages/wms/station/instances/outbound/operations/tips/scan-error-remind"
import type { ModalType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
import { TipType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import ConfigControlledModal from "@/pages/wms/station/widgets/config-controlled-modal"
import { MessageType } from "@/pages/wms/station/widgets/message"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import ChoosePickingTaskTip from "../../../../widgets/ChoosePickingTaskTip"

export interface TipsHandlerProps<T> {
    tipType: TipType
    data?: T[]
    type?: ModalType
    duration?: number
    tipCode?: string
}

// 定义组件 ref 的类型
interface ComponentRef {
    abnormalReportReason?: string
    pickNum?: number
    isError?: boolean
    dataSource?: Array<{
        id: string
        toBeOperatedQty: number
    }>
    totalToBeRequiredQty?: number
}

// 定义提示配置接口
interface TipConfig {
    component: FC<OperationProps<any, any>>
    handleSubmit?: (ref: RefObject<ComponentRef>) => Promise<void>
    modalType: TabActionModalType.NORMAL | TabActionModalType.FULL_SCREEN
    modalConfig?: TabAction["modalConfig"]
    handleClose?: () => void
}

// 定义选择拣选任务的数据类型
interface ChoosePickingTaskData {
    choosePickingTasks?: Array<{
        id: string
        [key: string]: any
    }>
}

// 定义拣选任务记录类型
interface PickingTaskRecord {
    id: string
    [key: string]: any
}

const ChoosePickingTaskTipWrapper: FC<OperationProps<ChoosePickingTaskData, PickingTaskRecord>> = (props) => {
    const { value } = props
    const { choosePickingTasks = [] } = value || {}
    const { onActionDispatch, message } = useWorkStation()
    
    const handleConfirm = async (record: PickingTaskRecord) => {
        const { code, msg } = await onActionDispatch({
            eventCode: TipType.CHOOSE_PICKING_TASK_TIP,
            data: record
        })

        if (code !== "0") {
            message?.({
                type: MessageType.ERROR,
                content: msg
            })
            return
        }
    }

    return (
        <ChoosePickingTaskTip
            value={choosePickingTasks}
            handleConfirm={handleConfirm}
        />
    )
}

// 定义提示数据类型
interface TipData {
    tipType: TipType
    data?: Record<string, any>
    type?: ModalType
    duration?: number
    tipCode?: string
}

const TipsHandler: FC<OperationProps<TipData[], any>> = (props) => {
    const { t } = useTranslation()
    const { onActionDispatch, message, workStationEvent } = useWorkStation()

    const value = workStationEvent?.tips || []
    const currentTip = value?.[0]
    const TipConfig: Record<TipType, TipConfig> = {
        [TipType.CHOOSE_PICKING_TASK_TIP]: {
            component: ChoosePickingTaskTipWrapper,
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("modal.selectSku"),
                footer: null
            }
        },
        [TipType.REPORT_ABNORMAL_TIP]: {
            component: ExceptionLog,
            handleSubmit: async (contentRef: RefObject<ComponentRef>) => {
                const current = contentRef.current
                if (!current) return
                
                const {
                    abnormalReportReason,
                    pickNum,
                    isError,
                    dataSource,
                    totalToBeRequiredQty
                } = current
                
                if (isError) return
                if (
                    abnormalReportReason === "LESS" &&
                    pickNum !== undefined &&
                    totalToBeRequiredQty !== undefined &&
                    pickNum >= totalToBeRequiredQty
                ) {
                    // 短拣数据异常
                    message?.({
                        type: MessageType.ERROR,
                        content: t("toast.pickingDataAbnormal")
                    })
                    return
                }

                const reportAbnormalTasks = (dataSource || []).map((item) => ({
                    taskId: item.id,
                    toBeOperatedQty: item.toBeOperatedQty
                }))

                await onActionDispatch({
                    eventCode: CustomActionType.REPORT_ABNORMAL,
                    data: {
                        abnormalReason: abnormalReportReason,
                        reportAbnormalTasks
                    }
                })
            },
            modalType: TabActionModalType.FULL_SCREEN,
            modalConfig: {
                title: t("button.abnormalRecord")
            }
        },
        [TipType.EMPTY_CONTAINER_HANDLE_TIP]: {
            component: EmptyContainerHandler,
            handleSubmit: async () => {
                // 空箱处理逻辑
            },
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("outbound.station.emptyContainer.handler.title"),
                footer: null,
                closable: false
            },
            handleClose: () => {
                // 空箱处理关闭逻辑
            }
        },
        [TipType.SEAL_CONTAINER_TIP]: {
            component: CloseContainer,
            handleSubmit: async () => {
                // 封箱处理逻辑
            },
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("outbound.station.sealContainer.remainder.title"),
                footer: null
            }
        },
        [TipType.SCAN_ERROR_REMIND_TIP]: {
            component: ScanErrorRemind,
            handleSubmit: async () => {
                // 扫描错误提醒逻辑
            },
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: "",
                footer: null,
                closable: false
            }
        },
        [TipType.FULL_CONTAINER_AUTO_OUTBOUND_TIP]: {
            component: MessageRemind,
            handleSubmit: async () => {
                // 整箱出库提醒逻辑
            },
            modalType: TabActionModalType.NORMAL,
            modalConfig: {}
        },
        [TipType.PICKING_VOICE_TIP]: {
            component: () => null,
            modalType: TabActionModalType.NORMAL,
            modalConfig: {}
        }
    }

    const handleClose = async () => {
        if (!currentTip?.tipCode) return
        
        await onActionDispatch({
            eventCode: "CLOSE_TIP",
            data: currentTip.tipCode
        })
    }

    const handleVoiceClose = async (result?: string) => {
        if (!currentTip) return
        
        await onActionDispatch({
            eventCode: "PICKING_VOICE_TIP",
            data: {
                tipType: currentTip.tipType,
                tipCode: currentTip.tipCode,
                result
            }
        })
    }

    const currentTipType = currentTip?.tipType
    const currentTipConfig = currentTipType && currentTipType in TipConfig 
        ? TipConfig[currentTipType as TipType] 
        : undefined

    const closeFn = currentTipConfig?.handleClose || handleClose

    const contentValue: Record<string, any> = {
        ...(currentTip?.data || {}),
        type: currentTip?.type,
        duration: currentTip?.duration || 3000,
        tipType: currentTip?.tipType,
        tipCode: currentTip?.tipCode,
        onActionDispatch,
        message
    }

    if (!currentTip || !currentTipConfig) {
        return null
    }

    return (
        <ConfigControlledModal
            config={currentTipConfig}
            handleClose={closeFn}
            handleVoiceClose={handleVoiceClose}
            visible={!!currentTip}
            contentValue={contentValue}
            byCloseStatus={[TipType.SCAN_ERROR_REMIND_TIP].includes(
                currentTip.tipType
            )}
        />
    )
}

export default TipsHandler
