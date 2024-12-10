import React from "react"
import type { ContainerSlotSpecsItem } from "@/pages/wms/station/event-loop/types"

/* 显示的货架层数 **/
const SHOW_LAYERS = 5

const Racks = (props: any) => {
    const {
        activeSlotCodes = [],
        containerCode,
        containerSpec,
        disabledSlotCodes = [],
        recommendSlotCodes = [],
        showAllSlots,
        backgroundColor = "#657AEA",
        onCustomActionDispatch
    } = props || {}

    const level = containerSpec?.containerSlotSpecs?.reduce(
        (pre: number, cur: ContainerSlotSpecsItem) => {
            return cur.locLevel > pre ? cur.locLevel : pre
        },
        0
    )

    const rows = [...new Array(level)].map((r, index) => index + 1)

    const shelfList = rows.map((row) => {
        return containerSpec?.containerSlotSpecs
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
            style={{
                // height: "100%",
                flex: 1,
                padding: "10px 10px 0",
                display: "flex",
                flexDirection: "column-reverse",
                width: "100%"
            }}
        >
            {list.map((item, index) => {
                const selectArr = item?.find((val: ContainerSlotSpecsItem) =>
                    activeSlotCodes?.includes(val.containerSlotSpecCode)
                )
                return (
                    <div
                        key={index}
                        className="d-flex flex-col-reverse items-center my-1 flex-1"
                        style={{
                            width: 100 + "%",
                            flexDirection: "initial",
                            border: "1px solid #000",
                            borderLeft: "none"
                        }}
                    >
                        {item &&
                            item.map(
                                (cell: ContainerSlotSpecsItem, i: number) => {
                                    const isActiveSlot =
                                        activeSlotCodes?.includes(
                                            cell.containerSlotSpecCode
                                        )
                                    const isDisabledSlot =
                                        disabledSlotCodes?.includes(
                                            cell.containerSlotSpecCode
                                        )
                                    const isRecommendSlot =
                                        recommendSlotCodes?.includes(
                                            cell.containerSlotSpecCode
                                        )
                                    return (
                                        <div
                                            key={i}
                                            className="relative d-flex flex-col flex-1 justify-between w-full"
                                            // onClick={(e) =>
                                            //     !isDisabledSlot &&
                                            //     onCustomActionDispatch &&
                                            //     onCustomActionDispatch(cell)
                                            // }
                                            style={{
                                                height: 100 + "%",
                                                cursor: isDisabledSlot
                                                    ? "not-allowed"
                                                    : "pointer",
                                                background: isActiveSlot
                                                    ? backgroundColor
                                                    : isDisabledSlot
                                                    ? "#CCC"
                                                    : "#f6f7fb",
                                                border: `1px solid ${
                                                    isRecommendSlot
                                                        ? "#657aea"
                                                        : ""
                                                }`,
                                                borderRight: "none",
                                                borderTop: "none",
                                                borderBottom: "none"
                                            }}
                                            data-testid={
                                                cell.containerSlotSpecCode
                                            }
                                            data-activeName={
                                                isActiveSlot ? "activeSlot" : ""
                                            }
                                        >
                                            <span className="d-flex flex-1 flex-col items-center justify-center w-full text-md leading-5 text-center">
                                                {isActiveSlot
                                                    ? // ? cell.containerSlotSpecCode
                                                      cell.level + cell.locBay
                                                    : ""}
                                                &nbsp;
                                            </span>
                                        </div>
                                    )
                                }
                            )}
                    </div>
                )
            })}
        </div>
    )
}

export default Racks
