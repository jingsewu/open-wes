import type { InputRef } from "antd"
import React, { useRef } from "react"
import SlotLayout from "./SlotLayout"
import WallIcon from "./WallIcon"
import SlotStatusMap from "./SlotStatusMap"
import Scan from "./Scan"
import { PutWallProps } from "./types"
import {
    PutWallDisplayStyle,
    DisplayOrder,
    PutWallSlotsItem,
    PutWallSlotStatus,
    ChooseArea
} from "@/pages/wms/station/event-loop/types"
import { useWorkStation } from "@/pages/wms/station/state"

const PutWall = (props: PutWallProps) => {
    const inputRef = useRef<InputRef>(null)
    const { onActionDispatch, workStationEvent, chooseArea } = useWorkStation()

    // 解构 props
    const {
        onSlotClick: propOnSlotClick,
        putWallDisplayStyle: propPutWallDisplayStyle,
        putWallViews: propPutWallViews,
        putWallTagConfigDTO: propPutWallTagConfigDTO,
        putWallStatusTextMap: propPutWallStatusTextMap,
        isActive: propIsActive,
        onLocationChange: propOnLocationChange
    } = props

    // 优先使用 props 中的数据，如果没有则从 WorkStationStore 获取
    const putWallViews =
        propPutWallViews || workStationEvent?.putWallArea?.putWallViews || []
    const putWallTagConfigDTO =
        propPutWallTagConfigDTO ||
        workStationEvent?.putWallArea?.putWallTagConfigDTO ||
        {}
    const putWallDisplayStyle =
        propPutWallDisplayStyle ||
        workStationEvent?.putWallArea?.putWallDisplayStyle
    const isActive =
        propIsActive !== undefined
            ? propIsActive
            : chooseArea === ChooseArea.putWallArea

    // 处理槽位点击函数 - 优先使用传入的 prop
    const onSlotClick =
        propOnSlotClick ||
        (async (item: PutWallSlotsItem) => {
            if (
                ![
                    PutWallSlotStatus.WAITING_SEAL,
                    PutWallSlotStatus.DISPATCH
                ].includes(item.putWallSlotStatus as PutWallSlotStatus)
            ) {
                return
            }

            const { code, msg } = await onActionDispatch({
                eventCode: "TAP_PUT_WALL_SLOT",
                data: {
                    putWallSlotCode: item.putWallSlotCode
                }
            })
        })

    // 播种墙状态文本映射 - 优先使用传入的 prop
    const putWallStatusTextMap = propPutWallStatusTextMap || {
        waitingBinding: "待绑定",
        waitingSeal: "待封箱",
        dispatch: "待派发",
        sealed: "已封箱",
        dispatched: "已派发"
    }

    if (isActive) {
        inputRef.current?.focus()
    }

    const handleInputFocus = () => {
        inputRef.current?.focus()
    }

    return (
        <div className="d-flex flex-col h-full" onClick={handleInputFocus}>
            <div className="d-flex items-center	justify-between mb-4">
                <WallIcon
                    putWallDisplayStyle={putWallDisplayStyle}
                    putWallViews={putWallViews}
                />
                <SlotStatusMap
                    putWallTagConfigDTO={putWallTagConfigDTO}
                    putWallStatusTextMap={putWallStatusTextMap}
                />
            </div>

            <div className="d-flex flex-1 gap-3">
                {putWallViews.map((item, index) => {
                    return putWallDisplayStyle === PutWallDisplayStyle.merge ||
                        item.active ? (
                        <SlotLayout
                            key={index}
                            putWallSlotView={item.putWallSlots}
                            rowReverse={
                                item.displayOrder === DisplayOrder.RIGHT_TO_LEFT
                            }
                            putWallTagConfigDTO={putWallTagConfigDTO}
                            onSlotClick={onSlotClick}
                            width={item.containerSpec?.width}
                        />
                    ) : null
                })}
            </div>
            <Scan ref={inputRef} />
        </div>
    )
}

export default PutWall
