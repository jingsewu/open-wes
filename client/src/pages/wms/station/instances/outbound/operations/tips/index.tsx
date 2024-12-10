import type { FC, RefObject } from "react"
import React from "react"
import { Translation, useTranslation } from "react-i18next"

import type { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
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
// import IntlMessages from "@/util/IntlMessages"
// import { objectToString } from "@/util/utils"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import ChoosePickingTaskTip from "../../../../widgets/ChoosePickingTaskTip"

export interface TipsHandlerProps<T> {
    tipType: TipType
    data?: T[]
    type?: ModalType
    duration?: number
    tipCode?: string
}

interface TipConfig {
    component: FC<any>
    handleSubmit?: (ref: RefObject<any>) => Promise<any>
    modalType: TabActionModalType.NORMAL | TabActionModalType.FULL_SCREEN
    modalConfig?: TabAction["modalConfig"]
    handleClose?: () => void
}

export const valueFilter = (
    data:
        | WorkStationEvent<{
              tips: TipsHandlerProps<any>[]
          }>
        | undefined
): TipsHandlerProps<any>[] => {
    if (!data) return []

    return data.tips
}

const ChoosePickingTaskTipWrapper = (props: OperationProps<any, any>) => {
    const { value } = props
    const { choosePickingTasks = [], onCustomActionDispatch, message } = value
    const handleConfirm = async (record: any) => {
        const { code, msg } = await onCustomActionDispatch({
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

const TipsHandler = (props: OperationProps<TipsHandlerProps<any>[], any>) => {
    const { t } = useTranslation()
    const { value = [], onCustomActionDispatch, message } = props
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
            handleSubmit: async (contentRef) => {
                // @ts-ignore
                const {
                    abnormalReportReason,
                    pickNum,
                    isError,
                    dataSource,
                    totalToBeRequiredQty
                } = contentRef.current
                if (isError) return
                if (
                    abnormalReportReason === "LESS" &&
                    pickNum >= totalToBeRequiredQty
                ) {
                    // 短拣数据异常
                    message?.({
                        type: MessageType.ERROR,
                        content: t("toast.pickingDataAbnormal")
                    })
                    return
                }

                const reportAbnormalTasks = dataSource.map((item: any) => ({
                    taskId: item.id,
                    toBeOperatedQty: item.toBeOperatedQty
                }))

                await onCustomActionDispatch({
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
            handleSubmit: async () => {},
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: "空箱处理",
                footer: null,
                closable: false
            },
            handleClose: () => {}
        },
        [TipType.SEAL_CONTAINER_TIP]: {
            component: CloseContainer,
            handleSubmit: async () => {},
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: "封箱提醒",
                footer: null
            }
        },
        [TipType.SCAN_ERROR_REMIND_TIP]: {
            component: ScanErrorRemind,

            handleSubmit: async () => {},
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: "",
                footer: null,
                closable: false
            }
        },
        [TipType.FULL_CONTAINER_AUTO_OUTBOUND_TIP]: {
            component: MessageRemind,
            handleSubmit: async () => {},
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
        await onCustomActionDispatch({
            eventCode: "CLOSE_TIP",
            data: currentTip.tipCode
        })
    }

    const handleVoiceClose = async (result?: string) => {
        await onCustomActionDispatch({
            eventCode: "PICKING_VOICE_TIP",
            data: {
                tipType: currentTip?.tipType,
                tipCode: currentTip?.tipCode,
                result
            }
        })
    }

    const closeFn = TipConfig[currentTip?.tipType]?.handleClose
        ? (TipConfig[currentTip?.tipType].handleClose as () => void)
        : handleClose

    const contentValue = {
        ...(currentTip?.data || {}),
        type: currentTip?.type,
        duration: currentTip?.duration || 3000,
        tipType: currentTip?.tipType,
        tipCode: currentTip?.tipCode,
        onCustomActionDispatch,
        message
    }

    return (
        <ConfigControlledModal
            config={{
                ...TipConfig[currentTip?.tipType]
            }}
            handleClose={closeFn}
            handleVoiceClose={handleVoiceClose}
            visible={!!currentTip}
            contentValue={contentValue}
            byCloseStatus={[TipType.SCAN_ERROR_REMIND_TIP].includes(
                currentTip?.tipType
            )}
        />
    )
}

export default TipsHandler
