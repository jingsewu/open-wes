import React from "react"
import type {ContainerSlotSpecsItem} from "@/pages/wms/station/event-loop/types"
import Shelf from "./Shelf"
/* 显示的货架层数 **/
const SHOW_LAYERS = 5

const ShelfModel = (props: any) => {
    const {
        containerSlotSpecs,
        activeSlotCodes,
        disabledSlotCodes,
        recommendSlotCodes,
        showAllSlots,
        showLevel = true,
        onActionDispatch
    } = props
    console.log("containerSlotSpecs", containerSlotSpecs)
    const level = containerSlotSpecs
        ? containerSlotSpecs.reduce(
            (pre: number, cur: ContainerSlotSpecsItem) => {
                return cur.locLevel > pre ? cur.locLevel : pre
            },
            0
        )
        : 0

    const rows = [...new Array(level)].map((r, index) => index + 1)

    const shelfList = rows.map((row) => {
        return containerSlotSpecs
            ?.filter((c: ContainerSlotSpecsItem) => c.locLevel === row)
            .sort(
                (c: ContainerSlotSpecsItem, c1: ContainerSlotSpecsItem) =>
                    c.locBay - c1.locBay
            )
    })

    const selectLevel =
        shelfList.find((item) => {
            return item?.find((i: ContainerSlotSpecsItem) =>
                activeSlotCodes?.includes(i.containerSlotSpecCode)
            )
        })?.[0]?.locLevel || 0

    /** 当货架层数大于5时，只显示5层货架
     * 处于上面3层与下面3层之间的货架，会显示待拣槽口的上两层 + 下两层。
     * 处于最上面3层的货架就显示最上面的5层货架，处于最下面3层的货架则显示最下面的5层货架
     */
    const showLevels = shelfList.filter((item) => {
        if (!item) return false
        return selectLevel > 3 && selectLevel < shelfList.length - 3
            ? Math.abs(item[0].locLevel - selectLevel) < 3
            : selectLevel <= 3
                ? item[0].locLevel <= SHOW_LAYERS
                : item[0].locLevel > shelfList.length - SHOW_LAYERS
    })

    const list = activeSlotCodes && !showAllSlots ? showLevels : shelfList

    return (
        <div
            className="d-flex flex-1 justify-around	w-full overflow-hidden"
            style={{flexDirection: "column-reverse"}}
        >
            {list.map((item, index) => {
                const selectArr = item?.find((val: ContainerSlotSpecsItem) =>
                    activeSlotCodes?.includes(val.containerSlotSpecCode)
                )
                return (
                    <div
                        className="d-flex gap-1"
                        key={index}
                        style={{height: 100 / list.length + "%"}}
                    >
                        {}
                        <Shelf
                            item={item}
                            index={index}
                            activeSlotCodes={activeSlotCodes}
                            disabledSlotCodes={disabledSlotCodes}
                            recommendSlotCodes={recommendSlotCodes}
                            onActionDispatch={onActionDispatch}
                            type="CONTAINER"
                        />

                        {}
                    </div>
                )
            })}
        </div>
    )
}

export default ShelfModel
