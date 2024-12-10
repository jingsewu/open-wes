/**
 * 当前工作站实例的布局
 */
import React, { memo } from "react"

import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { valueFilter as containerFilter } from "@/pages/wms/station/instances/outbound/operations/containerHandler"
import { valueFilter as pickFilter } from "@/pages/wms/station/instances/outbound/operations/pickingHandler"
import { valueFilter as putWallFilter } from "@/pages/wms/station/instances/outbound/operations/putWallHandler"
import { valueFilter as tipsFilter } from "@/pages/wms/station/instances/outbound/operations/tips"

import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import type {
    WorkStationEvent,
    WorkStationInfo
} from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import { MessageType } from "@/pages/wms/station/widgets/message"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"

export interface OutBoundLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationEvent<any>
    workStationInfo: WorkStationInfo
}

const OutBoundLayout = (props: OutBoundLayoutProps) => {
    const { onCustomActionDispatch, message } = props
    const changeAreaHandler = async (type: string) => {
        const { code, msg } = await onCustomActionDispatch({
            eventCode: CustomActionType.CHOOSE_AREA,
            data: type
        })
        if (code === "-1") {
            message?.({
                type: MessageType.ERROR,
                content: msg
            })
            return
        }
    }

    return (
        <>
            <div className="d-flex flex-col	 h-full w-full">
                <div className="d-flex mb-4 w-full ">
                    {/* 容器区域 */}
                    <div
                        className="d-flex mr-5 bg-white overflow-hidden"
                        style={{ flex: 3 }}
                    >
                        <ComponentWrapper
                            // className="w-full p-4 max-w-xs"
                            style={{
                                width: "100%",
                                padding: 12,
                                maxWidth: 550
                            }}
                            type={ChooseArea.workLocationArea}
                            Component={
                                OPERATION_MAP[ChooseArea.workLocationArea]
                            }
                            valueFilter={containerFilter}
                        />
                    </div>
                    {/* 商品信息 */}
                    <div
                        className="bg-white overflow-hidden"
                        style={{ flex: 7 }}
                    >
                        <ComponentWrapper
                            style={{ width: "100%", padding: 12 }}
                            type={ChooseArea.skuArea}
                            Component={OPERATION_MAP[ChooseArea.skuArea]}
                            valueFilter={pickFilter}
                            changeAreaHandler={changeAreaHandler.bind(
                                null,
                                ChooseArea.skuArea
                            )}
                        />
                    </div>
                </div>
                {/* 播种墙 */}
                <div className="d-flex flex-grow flex-col bg-white">
                    <div className="flex-1">
                        <ComponentWrapper
                            type={ChooseArea.putWallArea}
                            Component={OPERATION_MAP[ChooseArea.putWallArea]}
                            valueFilter={putWallFilter}
                            changeAreaHandler={changeAreaHandler.bind(
                                null,
                                ChooseArea.putWallArea
                            )}
                        />
                    </div>
                </div>
            </div>
            <ComponentWrapper
                type={ChooseArea.tips}
                Component={OPERATION_MAP[ChooseArea.tips]}
                valueFilter={tipsFilter}
                withWrapper={false}
            />
        </>
    )
}

export default memo(OutBoundLayout)
