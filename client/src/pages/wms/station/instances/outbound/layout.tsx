/**
 * 当前工作站实例的布局
 */
import React, { useCallback } from "react"

import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"

import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import type { WorkStationView } from "@/pages/wms/station/event-loop/types"
import { MessageType } from "@/pages/wms/station/widgets/message"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { useWorkStation, observer } from "../../state"

export interface OutBoundLayoutProps {
    workStationEvent: WorkStationView<any>
}

const OutBoundLayout = observer((props: OutBoundLayoutProps) => {
    const { store, onActionDispatch, message } = useWorkStation()

    const chooseArea = store?.chooseArea
    const containerAreaIsActive = chooseArea === ChooseArea.workLocationArea
    const skuAreaIsActive = chooseArea === ChooseArea.skuArea
    const putWallAreaIsActive = chooseArea === ChooseArea.putWallArea

    const changeAreaHandler = useCallback(
        async (type: string) => {
            try {
                if (!onActionDispatch) {
                    return
                }
                const { code, msg } = await onActionDispatch({
                    eventCode: CustomActionType.CHOOSE_AREA,
                    data: type
                })
                if (code === "-1") {
                    message?.({
                        type: MessageType.ERROR,
                        content: msg
                    })
                }
            } catch (error) {
                message?.({
                    type: MessageType.ERROR,
                    content: error.message
                })
            }
        },
        [onActionDispatch, message]
    )

    const skuAreaChangeHandler = () => changeAreaHandler(ChooseArea.skuArea)
    const putWallAreaChangeHandler = () =>
        changeAreaHandler(ChooseArea.putWallArea)

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
                            style={{
                                width: "100%",
                                padding: 12,
                                maxWidth: 550
                            }}
                            type={ChooseArea.workLocationArea}
                            Component={
                                OPERATION_MAP[ChooseArea.workLocationArea]
                            }
                            isActive={containerAreaIsActive}
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
                            changeAreaHandler={skuAreaChangeHandler}
                            isActive={skuAreaIsActive}
                        />
                    </div>
                </div>
                {/* 播种墙 */}
                <div className="d-flex flex-grow flex-col bg-white">
                    <div className="flex-1">
                        <ComponentWrapper
                            type={ChooseArea.putWallArea}
                            Component={OPERATION_MAP[ChooseArea.putWallArea]}
                            changeAreaHandler={putWallAreaChangeHandler}
                            isActive={putWallAreaIsActive}
                        />
                    </div>
                </div>
            </div>
            <ComponentWrapper
                type={ChooseArea.tips}
                Component={OPERATION_MAP[ChooseArea.tips]}
                withWrapper={false}
            />
        </>
    )
})

export default OutBoundLayout
