import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import React from "react"
import { CustomActionType } from "../../customActionType"
import { Translation } from "react-i18next"

const taskConfig = () => {
    const taskConfigObj: TabAction = {
        name: <Translation>{(t) => t("button.abnormalRecord")}</Translation>,
        key: "exceptionRegistration",
        modalType: TabActionModalType.NORMAL,
        icon: "",
        permissions: [10206],
        testid: "abnormalRecord",
        disabled: (workStationEvent: any) => {
            if (!workStationEvent) return true
            return !workStationEvent?.scanCode
        },
        modalConfig: {
            title: (
                <Translation>{(t) => t("button.abnormalRecord")}</Translation>
            ),
            okText: (
                <Translation>{(t) => t("button.abnormalRecord")}</Translation>
            ),
            centered: true,
            width: 900
        },
        emitter: async (props: any) => {
            const { onCustomActionDispatch, workStationEvent } = props
            const inboundOrderId = workStationEvent?.orderArea?.find(
                (item: any) => item.active
            )?.orderId
            const { code, msg } = await onCustomActionDispatch({
                eventCode: CustomActionType.INBOUND_ABNORMAL_TIP,
                data: {
                    inboundOrderId,
                    skuCode: workStationEvent.scanCode,
                    batchAttributes:
                        workStationEvent.skuArea.pickingViews?.[0]
                            .skuBatchAttributeDTO.skuAttributes
                }
            })
        }
    }
    return taskConfigObj
}
export default taskConfig
