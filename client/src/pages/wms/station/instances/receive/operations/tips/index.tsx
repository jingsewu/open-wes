import type {OperationProps} from "@/pages/wms/station/instances/types"
import {TabActionModalType} from "@/pages/wms/station/tab-actions/types"
import ChoosePickingTaskTip from "@/pages/wms/station/widgets/ChoosePickingTaskTip"
import ChooseSkuCode from "@/pages/wms/station/widgets/ChooseSkuCode"
import ConfigControlledModal from "@/pages/wms/station/widgets/config-controlled-modal"
import React from "react"
import {CustomActionType} from "@/pages/wms/station/instances/receive/customActionType"
import {WorkStationView} from "@/pages/wms/station/event-loop/types"
import type {ModalType} from "@/pages/wms/station/instances/outbound/operations/tips/type"
import Abnormal from "./Abnormal"
import {useTranslation} from "react-i18next"
import {useWorkStation} from "@/pages/wms/station/state"

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (data: WorkStationView<any>) => {
    if (!data) return {}
    return {
        tips: data.tips,
    }
}

export enum TipType {
    /** 异常登记 */
    INBOUND_ABNORMAL_TIP = "INBOUND_ABNORMAL_TIP",
    /** 商品一品多批次弹窗 */
    SKU_ORDERS_OR_OWNER_CODES_TIP = "SKU_ORDERS_OR_OWNER_CODES_TIP",
    /** 商品一码多品弹窗 */
    BARCODE_2_MANY_SKU_CODE_TIP = "BARCODE_2_MANY_SKU_CODE_TIP"
}

interface TipsHandlerProps {
    tipType: TipType
    data?: string
    type?: ModalType
    duration?: number
    tipCode?: string
}

interface TipProps {
    tips: TipsHandlerProps[]
}

interface TipData {
    ownerCode: string
    lpnCode: string
    customerOrderNo: string
    id: string
    inboundPlanOrderId: string
    containerCode: string
    containerSpecCode: string
    containerSlotCode: string
    boxNo: string
    qtyRestocked: number
    qtyAccepted: number
    qtyUnreceived: number
    qtyAbnormal: number
    abnormalReason: string
    responsibleParty: string
    skuCode: string
    skuId: string
    skuName: string
    style: string
    color: string
    size: string
    brand: string
    batchAttributes: any
    extendFields: null
}

function Tips(props: OperationProps<TipProps, any>) {
    const {t} = useTranslation()
    const {value} = props
    const {onActionDispatch, message} = useWorkStation()
    const currentTip = value?.tips?.[0] as TipsHandlerProps

    const {data} = currentTip || {}
    const dataSource = JSON.parse(data || "[]")

    const TipConfig: Record<TipType, any> = {
        [TipType.INBOUND_ABNORMAL_TIP]: {
            component: Abnormal,
            handleSubmit: async (contentRef: any) => {
                // @ts-ignore

                await onActionDispatch({
                    eventCode: CustomActionType.INBOUND_ABNORMAL_CONFIRM,
                    data: contentRef.current
                })
            },
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("button.abnormalRecord")
            }
        },
        [TipType.SKU_ORDERS_OR_OWNER_CODES_TIP]: {
            component: (props: any) => (
                <ChoosePickingTaskTip
                    value={dataSource}
                    handleConfirm={handleConfirm}
                />
            ),
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("modal.selectTheCorrespondingTask"),
                width: 1000,
                footer: null,
                closable: false
            }
        },
        [TipType.BARCODE_2_MANY_SKU_CODE_TIP]: {
            component: (props: any) => (
                <ChooseSkuCode
                    value={dataSource}
                    handleConfirm={handleSkuConfirm}
                />
            ),
            modalType: TabActionModalType.NORMAL,
            modalConfig: {
                title: t("modal.selectSku"),
                width: 1000,
                footer: null,
                closable: false
            }
        }
    }

    const handleConfirm = async (value: TipData) => {
        await onActionDispatch?.({
            eventCode: CustomActionType.INBOUND_SCAN_BARCODE_2_MANY_SKU_CODE,
            data: value
        })
        // return !errorCode
    }

    const handleSkuConfirm = async (value: any) => {
        await onActionDispatch?.({
            eventCode: CustomActionType.INBOUND_SCAN_BARCODE_2_MANY_SKU_CODE,
            data: value
        })
    }

    const handleClose = async () => {
        await onActionDispatch({
            eventCode: "CLOSE_TIP",
            data: currentTip?.tipCode
        })
    }

    const closeFn = TipConfig[currentTip?.tipType]?.handleClose
        ? (TipConfig[currentTip?.tipType].handleClose as () => void)
        : handleClose

    const contentValue = {
        // ...(currentTip?.data || {}),
        data: currentTip?.data,
        type: currentTip?.type,
        duration: currentTip?.duration || 3000,
        tipType: currentTip?.tipType,
        tipCode: currentTip?.tipCode,
        onActionDispatch: onActionDispatch,
        message
    }

    return (
        <ConfigControlledModal
            config={{
                ...TipConfig[currentTip?.tipType]
            }}
            handleClose={closeFn}
            visible={!!currentTip}
            contentValue={contentValue}
        />
    )
}

export default Tips
