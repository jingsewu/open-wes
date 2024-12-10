import React, { useMemo } from "react"

import SlotItem from "./SlotItem"
import {
    putWallSlotsItem,
    PutWallTagConfigDTO
} from "@/pages/wms/station/event-loop/types"

interface Props {
    putWallSlotView: putWallSlotsItem[]
    width?: number
    rowReverse?: boolean
    putWallTagConfigDTO: PutWallTagConfigDTO
    onSlotClick?: (data: putWallSlotsItem) => void
}

const MAX_SLOT_LEVEL = 7

const SlotLayout: React.FC<Props> = (props) => {
    const {
        putWallSlotView = [],
        width = 1,
        rowReverse,
        putWallTagConfigDTO,
        onSlotClick
    } = props

    // 把数据转化成二维数组并进行排序，再flex布局生成播种墙视图
    const matrixList = useMemo(() => {
        return (Array.from({ length: MAX_SLOT_LEVEL }) as putWallSlotsItem[][])
            .reduce((prev, curr, index) => {
                const row = putWallSlotView.filter(
                    (item) => item.locLevel === index + 1
                )
                if (row.length) {
                    prev.push(row.sort((a, b) => a.locBay - b.locBay))
                }
                return prev
            }, [] as putWallSlotsItem[][])
            .sort((a, b) => b[0].locLevel - a[0].locLevel)
    }, [putWallSlotView])

    return (
        <div
            className="d-flex flex-col w-full h-full gap-2 p-2"
            // className={cx('layout-grid')}
            style={{ backgroundColor: "#f8f8f8", flex: width }}
        >
            {matrixList.map((row, index) => {
                return (
                    <div
                        // className={`d-flex flex-wrap items-center h-full gap-2 `}
                        className={`d-flex flex-wrap 	items-center h-full gap-2
                        ${rowReverse ? "flex-row-reverse" : ""}
                        `}
                        key={index}
                    >
                        {row.map((item, i) => (
                            <SlotItem
                                key={i}
                                data={item}
                                putWallTagConfigDTO={putWallTagConfigDTO}
                                onSlotClick={onSlotClick}
                            />
                        ))}
                    </div>
                )
            })}
        </div>
    )
}

export default SlotLayout
