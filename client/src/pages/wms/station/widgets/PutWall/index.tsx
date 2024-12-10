import type { InputRef } from "antd"
import React, { useRef } from "react"
import SlotLayout from "./SlotLayout"
import WallIcon from "./WallIcon"
import SlotStatusMap from "./SlotStatusMap"
import Scan from "./Scan"
import { PutWallProps } from "./types"
import {
    PutWallDisplayStyle,
    DisplayOrder
} from "@/pages/wms/station/event-loop/types"

const PutWall = (props: PutWallProps) => {
    const inputRef = useRef<InputRef>(null)
    const {
        putWallViews = [],
        putWallTagConfigDTO = {},
        putWallDisplayStyle,
        putWallStatusTextMap = {},
        isActive,
        onSlotClick
    } = props

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
