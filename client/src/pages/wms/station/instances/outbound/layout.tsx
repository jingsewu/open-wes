/**
 * 当前工作站实例的布局
 */
import React from "react"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import type { WorkStationView } from "@/pages/wms/station/event-loop/types"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP, LAYOUT_STYLES } from "./config"
import { observer } from "../../state"
import { useOutboundLayout } from "./hooks"

export interface OutBoundLayoutProps {
    workStationEvent: WorkStationView<any>
}

const OutBoundLayout = observer((props: OutBoundLayoutProps) => {
    const { changeAreaHandler, containerAreaIsActive, skuAreaIsActive, putWallAreaIsActive } = useOutboundLayout()

    return (
        <>
            <div className="d-flex flex-col	 h-full w-full">
                <div className="d-flex mb-4 w-full ">
                    {/* 容器区域 */}
                    <div
                        className="d-flex mr-5 bg-white overflow-hidden"
                        style={{ flex: LAYOUT_STYLES.CONTAINER_AREA.flex }}
                    >
                        <ComponentWrapper
                            style={{
                                width: "100%",
                                padding: LAYOUT_STYLES.CONTAINER_AREA.padding,
                                maxWidth: LAYOUT_STYLES.CONTAINER_AREA.maxWidth
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
                        style={{ flex: LAYOUT_STYLES.SKU_AREA.flex }}
                    >
                        <ComponentWrapper
                            style={{ width: "100%", padding: LAYOUT_STYLES.SKU_AREA.padding }}
                            type={ChooseArea.skuArea}
                            Component={OPERATION_MAP[ChooseArea.skuArea]}
                            changeAreaHandler={() => changeAreaHandler(ChooseArea.skuArea)}
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
                            changeAreaHandler={() => changeAreaHandler(ChooseArea.putWallArea)}
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
