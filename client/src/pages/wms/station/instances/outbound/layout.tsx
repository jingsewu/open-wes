/**
 * 当前工作站实例的布局
 */
import React from "react"

import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { valueFilter as containerFilter } from "@/pages/wms/station/instances/outbound/operations/containerHandler"
import { valueFilter as pickFilter } from "@/pages/wms/station/instances/outbound/operations/pickingHandler"
import { valueFilter as putWallFilter } from "@/pages/wms/station/instances/outbound/operations/putWallHandler"
import { valueFilter as tipsFilter } from "@/pages/wms/station/instances/outbound/operations/tips"

import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import type { WorkStationView } from "@/pages/wms/station/event-loop/types"
import { MessageType } from "@/pages/wms/station/widgets/message"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { useWorkStation, useWorkStationArea, observer } from "../../state"

export interface OutBoundLayoutProps {
    workStationEvent: WorkStationView<any>
}

const OutBoundLayout = observer((props: OutBoundLayoutProps) => {
    const { store, onActionDispatch, message } = useWorkStation()
    const { setChooseArea, setError } = store

    // 区域状态管理
    const containerArea = useWorkStationArea(ChooseArea.workLocationArea)
    const skuArea = useWorkStationArea(ChooseArea.skuArea)
    const putWallArea = useWorkStationArea(ChooseArea.putWallArea)

    const changeAreaHandler = async (type: string) => {
        try {
            const { code, msg } = await onActionDispatch({
                eventCode: CustomActionType.CHOOSE_AREA,
                data: type
            })
            if (code === "-1") {
                setError(msg)
                message?.({
                    type: MessageType.ERROR,
                    content: msg
                })
            } else {
                setChooseArea(type as ChooseArea)
            }
        } catch (error) {
            setError(error.message)
            message?.({
                type: MessageType.ERROR,
                content: error.message
            })
        }
    }

    return (
        <>
            <div className="d-flex flex-col	 h-full w-full">
                <div className="d-flex mb-4 w-full ">
                    {/* 容器区域 */}
                    <div
                        className={`d-flex mr-5 bg-white overflow-hidden ${
                            containerArea.isActive ? "border-primary" : ""
                        }`}
                        style={{ flex: 3 }}
                    >
                        <ComponentWrapper
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
                        className={`bg-white overflow-hidden ${
                            skuArea.isActive ? "border-primary" : ""
                        }`}
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
                    <div
                        className={`flex-1 ${
                            putWallArea.isActive ? "border-primary" : ""
                        }`}
                    >
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
})

export default OutBoundLayout
