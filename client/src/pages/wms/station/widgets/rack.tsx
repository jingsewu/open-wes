import React from "react"
import type { CustomActionResponse } from "@/pages/wms/station/instances/types"

interface ContainerSlotSpec {
    bay: string
    level: string
    containerSlotSpecCode: string
    face: string
    subContainerLevel: string
    locLevel: number
    locBay: number
}

export interface RackProps {
    onCustomActionDispatch: (value: any) => Promise<CustomActionResponse>
    containerSlotList: ContainerSlotSpec[][]
    activeSlotCodes: string[]
}

const Rack = (props: RackProps) => {
    const { containerSlotList, activeSlotCodes, onCustomActionDispatch } = props
    return (
        <div className="d-flex flex-col-reverse flex-1 justify-around w-full overflow-hidden">
            {containerSlotList.map((item, index) => {
                const selectArr = item.filter((val) =>
                    activeSlotCodes.includes(val.containerSlotSpecCode)
                )
                return (
                    <div
                        className="d-flex"
                        // className={cx("seeding-flex")}
                        key={index}
                        style={{
                            height: 100 / containerSlotList.length + "%"
                        }}
                    >
                        <div
                            className="d-flex items-center justify-center w-40 text-xl	font-bold border-2 border-solid	border-r-0	rounded-l border-light"
                            // className={cx("seeding-index")}
                            style={{
                                background:
                                    selectArr.length !== 0
                                        ? "#1890FF"
                                        : "#f6f7fb",
                                border: `2px solid ${
                                    selectArr.length !== 0
                                        ? "#004CAA"
                                        : "#e8e8e8"
                                }`,
                                color:
                                    selectArr.length !== 0 ? "#fff" : "#999999"
                            }}
                        >
                            {/* {index + 1} */}
                            {item[0].level}
                        </div>
                        <div
                            key={index}
                            // className={cx("small-box")}
                            className="d-flex flex-col-reverse items-center"
                            style={{
                                width: 100 + "%",
                                flexDirection: "initial"
                            }}
                        >
                            {item &&
                                item.map((cell, i) => {
                                    return (
                                        <div
                                            key={i}
                                            // className={cx("seeding-item")}
                                            className="relative d-flex flex-col justify-between w-full p-1 text-white border-4 border-gray-200	"
                                            onClick={(e) =>
                                                onCustomActionDispatch &&
                                                onCustomActionDispatch(cell)
                                            }
                                            style={{
                                                height: 100 + "%",
                                                background:
                                                    activeSlotCodes.includes(
                                                        cell.containerSlotSpecCode
                                                    )
                                                        ? // containerSlotSpecCode ===
                                                          // cell.containerSlotSpecCode
                                                          "#1890FF"
                                                        : "#f6f7fb",
                                                border: `2px solid ${
                                                    // containerSlotSpecCode ===
                                                    // cell.containerSlotSpecCode
                                                    activeSlotCodes.includes(
                                                        cell.containerSlotSpecCode
                                                    )
                                                        ? "#004CAA"
                                                        : "#e8e8e8"
                                                }`
                                            }}
                                        >
                                            <span
                                                className="d-flex flex-1 flex-col items-center justify-center w-full text-md text-center"
                                                // className={cx(
                                                //     "item-span",
                                                //     "item-status"
                                                // )}
                                            >
                                                {
                                                    //     containerSlotSpecCode ===
                                                    // cell.containerSlotSpecCode
                                                    activeSlotCodes.includes(
                                                        cell.containerSlotSpecCode
                                                    )
                                                        ? cell.containerSlotSpecCode
                                                        : //  i + 1
                                                          ""
                                                }
                                                &nbsp;
                                            </span>
                                        </div>
                                    )
                                })}
                        </div>
                    </div>
                )
            })}
        </div>
    )
}

export default Rack
