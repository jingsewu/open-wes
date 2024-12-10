import type { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type {
    CustomActionResponse,
    OperationProps
} from "@/pages/wms/station/instances/types"
import classNames from "classnames/bind"
// import { pick } from "lodash-es"
import React from "react"
import style from "./styles.module.scss"
const cx = classNames.bind(style)
export interface MaterialProps {
    onCustomActionDispatch: (value: any) => Promise<CustomActionResponse>
    arrivedContainer: subContainerInterface
    allowContainer?: boolean
    level: number
}
interface subContainerInterface {
    containerSlotSpecCode?: string
    face?: string
    level?: number
    bay?: number
    containerCode: string
    activeSlotCodes?: string[]
    containerSpec: ContainerSpec
}

interface ContainerSpec {
    containerSlotSpecs: SubContainerListFace[]
}
interface SubContainerListFace {
    bay: string
    level: string
    containerSlotSpecCode: string
    locLevel: number
    locBay: number
}

export interface MaterialConfirmProps {
    skuCode: string
}
const MaterialRack = (props: MaterialProps) => {
    const {
        onCustomActionDispatch,
        arrivedContainer,
        allowContainer = true,
        level
    } = props
    const {
        activeSlotCodes = [],
        containerCode,
        containerSpec
    } = arrivedContainer || {}
    const rows = [...new Array(level)].map((r, index) => index + 1)
    const shelfLit = rows.map((row) => {
        return containerSpec?.containerSlotSpecs
            .filter((c) => c.locLevel === row)
            .sort((c, c1) => c.locBay - c1.locBay)
    })
    const toBePickdSlot = containerSpec?.containerSlotSpecs.find((item) =>
        activeSlotCodes?.includes(item.containerSlotSpecCode)
    )
    return (
        <div className="d-flex flex-col items-center w-full h-full">
            <div className="w-full mb-3">
                <div
                    className="d-flex py-2 w-full mb-0.5"
                    style={{ backgroundColor: "#F8F8F8" }}
                >
                    <div className="w-1/4 text-center">容器</div>
                    <div className="border-left w-1/4 text-center">面</div>
                    <div className="border-left w-1/4 text-center">层</div>
                    <div className="border-left border-l w-1/4 text-center">
                        序号
                    </div>
                </div>
                <div
                    className="d-flex py-2 w-full"
                    style={{ backgroundColor: "#F8F8F8" }}
                >
                    <div className="w-1/4 text-center">
                        {containerCode || 0}
                    </div>
                    <div className="border-left w-1/4 text-center">
                        {arrivedContainer?.face || 0}
                    </div>
                    <div className="border-left w-1/4 text-center">
                        {toBePickdSlot?.level || 0}
                    </div>
                    <div className="border-left border-l w-1/4 text-center">
                        {toBePickdSlot?.bay || 0}
                    </div>
                </div>
            </div>
            <div
                className="d-flex flex-1 justify-around	w-full overflow-hidden"
                style={{ flexDirection: "column-reverse" }}
            >
                {shelfLit.map((item, index) => {
                    const selectArr = item?.filter((val) =>
                        activeSlotCodes?.includes(val.containerSlotSpecCode)
                    )
                    return (
                        <div
                            className="d-flex"
                            key={index}
                            style={{ height: 100 / shelfLit.length + "%" }}
                        >
                            <div
                                className="d-flex items-center justify-center w-40 text-xl	font-bold border-2 border-solid	border-r-0	rounded-l border-light"
                                style={{
                                    background:
                                        selectArr?.length !== 0
                                            ? "#1890FF"
                                            : "#f6f7fb",
                                    border: `2px solid ${
                                        selectArr?.length !== 0
                                            ? "#004CAA"
                                            : "#e8e8e8"
                                    }`,
                                    color:
                                        selectArr?.length !== 0
                                            ? "#fff"
                                            : "#999999"
                                }}
                            >
                                {item?.[0].level}
                            </div>
                            <div
                                key={index}
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
                                                className="relative d-flex flex-col justify-between w-full p-1 text-white border-4 border-gray-200	"
                                                onClick={(e) =>
                                                    onCustomActionDispatch &&
                                                    onCustomActionDispatch(cell)
                                                }
                                                style={{
                                                    height: 100 + "%",
                                                    background:
                                                        activeSlotCodes?.includes(
                                                            cell.containerSlotSpecCode
                                                        )
                                                            ? "#1890FF"
                                                            : "#f6f7fb",
                                                    border: `2px solid ${
                                                        activeSlotCodes?.includes(
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
                                                        //     arrivedContainer.containerSlotSpecCode ===
                                                        // cell.containerSlotSpecCode
                                                        activeSlotCodes?.includes(
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
        </div>
    )
}

// export const valueFilter = (
//     data: WorkStationEvent<any>
// ): OperationProps<MaterialProps, MaterialConfirmProps>["value"] => {
//     return pick(data, ["skuInfo"])
// }
export default MaterialRack
