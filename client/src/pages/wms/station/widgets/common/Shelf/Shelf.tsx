import React from "react"
import type { ContainerSlotSpecsItem } from "@/pages/wms/station/event-loop/types"

const Shelf = ({
    index,
    item,
    activeSlotCodes,
    disabledSlotCodes,
    recommendSlotCodes,
    onCustomActionDispatch,
    type
}: any) => {
    return (
        <div
            key={index}
            className="d-flex flex-col-reverse items-center"
            style={{
                width: 100 + "%",
                flexDirection: "initial"
            }}
        >
            {item &&
                item.map((cell: ContainerSlotSpecsItem, i: number) => {
                    const isActiveSlot = activeSlotCodes?.includes(
                        cell.containerSlotSpecCode
                    )
                    const isDisabledSlot = disabledSlotCodes?.includes(
                        cell.containerSlotSpecCode
                    )
                    const isRecommendSlot = recommendSlotCodes?.includes(
                        cell.containerSlotSpecCode
                    )
                    return (
                        <div
                            key={i}
                            className="relative d-flex flex-col justify-between w-full p-1 text-white rounded-md"
                            onClick={(e) =>
                                !isDisabledSlot &&
                                onCustomActionDispatch &&
                                onCustomActionDispatch(cell)
                            }
                            style={{
                                height: 100 + "%",
                                cursor: isDisabledSlot
                                    ? "not-allowed"
                                    : "pointer",
                                background: isActiveSlot
                                    ? "#1890ff"
                                    : isDisabledSlot
                                    ? "#CCC"
                                    : "#f6f7fb",
                                border: `2px solid ${
                                    isRecommendSlot ? "#657aea" : ""
                                }`
                            }}
                            data-testid={cell.containerSlotSpecCode}
                            data-activeName={isActiveSlot ? "activeSlot" : ""}
                        >
                            <span className="d-flex flex-1 flex-col items-center justify-center w-full text-md leading-5 text-center">
                                {isActiveSlot
                                    ? // ? cell.containerSlotSpecCode
                                      type === "CONTAINER"
                                        ? cell.containerSlotSpecCode
                                        : cell.level + cell.locBay
                                    : ""}
                                &nbsp;
                            </span>
                        </div>
                    )
                })}
        </div>
    )
}

export default Shelf
