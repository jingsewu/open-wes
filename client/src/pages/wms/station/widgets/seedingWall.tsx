// import type { Property } from "@antv/x6/lib/types/csstype"
import classNames from "classnames/bind"
import React from "react"

import { BusinessType } from "@/pages/wms/station/widgets/types"
import type {
    PutWallSlotView,
    PutWallViews
} from "@/pages/wms/station/widgets/wall"

import style from "./styles.module.scss"

const cx = classNames.bind(style)
export const ShelvesStatus = {
    UNAVAILABLE: {
        bgColor: "#FAFAFA",
        color: "#B3B3B3",
        borderColor: "#E8E8E8",
        text: ""
    },
    FULL: {
        bgColor: "#FAFAFA",
        color: "#B3B3B3",
        borderColor: "#E8E8E8",
        text: ""
    },
    FREE: {
        bgColor: "#1BB55C",
        color: "#fff",
        borderColor: "#0E8F48",
        text: "待放箱"
        //  < IntlMessages id="workstaion.inbound.text.waitingBox" />
    },
    AVAILABLE: {
        bgColor: "#1BB55C",
        color: "#fff",
        borderColor: "#0E8F48",
        text: ""
    },
    BOUND: {
        bgColor: "#1890FF",
        color: "#fff",
        borderColor: "#004CAA",
        text: "完成"
        // <IntlMessages id="workstaion.outbound.text.complete" />
    },
    ABNORMAL: {
        bgColor: "#FE6668",
        color: "#fff",
        borderColor: "#CF1322",
        text: "搬箱异常"
        // <IntlMessages id="workstaion.outbound.text.abnormal" />
    }
}
let scaleSize = 1
export interface SeedingConfirmProps {
    skuCode: string
}

const SeedingWall = (
    props: PutWallViews & {
        isShelves?: boolean
        putWallViewsLength?: number
        noStatusText?: boolean
        isFull?: boolean
        onChange?: (value: any) => any
        isStockTake?: boolean
        getClickStatus?: (cell: PutWallSlotView) => boolean
    }
) => {
    const {
        location,
        bay,
        isShelves,
        onChange,
        level,
        putWallSlots = [],
        putWallViewsLength,
        isStockTake,
        isFull = false,
        noStatusText = false,
        getClickStatus
    } = props
    const project = JSON.parse(localStorage.getItem("project") || "{}")?.project
    const bays = [...new Array(bay)].map((r) => r + 1)
    const putWallSlotList = bays.map((bay) => {
        return putWallSlots
            .filter((c) => c.bay === bay)
            .sort((c, c1) => c.level - c1.level)
    })
    const max = bay > level ? bay : level

    switch (max) {
        case 7:
            scaleSize = 0.6
            break
        case 6:
            scaleSize = 0.3
            break
        case 5:
            scaleSize = 0.8
            break
        default:
            scaleSize = 1
            break
    }

    const shelvesChange = (cell: PutWallSlotView) => {
        if (cell.active || cell?.containerDesc?.containerCode) {
            onChange && onChange(cell)
            return
        }
        if (["FREE", "BOUND"].includes(cell.slotStatus)) {
            onChange && onChange(cell)
        }
    }
    const showWallSlotDesc =
        level === 7 || bay === 7
            ? putWallViewsLength === 2
                ? false
                : true
            : true
    /* eslint-disable complexity */
    return (
        <div
            className={cx("seeding-box")}
            style={{
                flexDirection: location === "LEFT" ? "row-reverse" : "row"
            }}
        >
            {putWallSlotList.map((item, index) => {
                return (
                    <div
                        key={index}
                        className={cx("small-box")}
                        style={{
                            width: 100 / putWallSlotList.length + "%"
                        }}
                    >
                        {item &&
                            item.map((cell, i) => {
                                const {
                                    backgroundColor,
                                    borderColor = "#e8e8e8",
                                    text = "",
                                    textColor,
                                    breathingLamp
                                } = cell
                                const statusObj =
                                    cell.active === undefined
                                        ? ShelvesStatus[
                                              (cell.slotStatus ||
                                                  "UNAVAILABLE") as keyof typeof ShelvesStatus
                                          ]
                                        : null
                                const defaultBg = cell.active
                                    ? "#1890FF"
                                    : cell.active === undefined
                                    ? statusObj?.bgColor
                                    : "#FAFAFA"
                                const defaultBorder = `2px solid ${
                                    cell.active
                                        ? "#004CAA"
                                        : cell.active === undefined
                                        ? statusObj?.borderColor
                                        : "#E8E8E8"
                                }`
                                return isShelves ? (
                                    <div
                                        key={i}
                                        className={cx(
                                            "seeding-item",
                                            "seeding-shelves",
                                            "seeding-item__default",
                                            {
                                                "seeding-item__active":
                                                    cell.active,
                                                "seeding-item__sourceContainer":
                                                    cell.active &&
                                                    cell.businessType ===
                                                        BusinessType.SOURCE,
                                                "seeding-item__targetContainer":
                                                    cell.active &&
                                                    cell.businessType ===
                                                        BusinessType.TARGET
                                            }
                                        )}
                                        onClick={(e) => shelvesChange(cell)}
                                        style={{
                                            height: 100 / item.length + "%",
                                            background:
                                                cell.active &&
                                                cell.businessType ===
                                                    BusinessType.TARGET
                                                    ? "#1bb55c"
                                                    : defaultBg,
                                            color: cell.active
                                                ? "#FFFFFF"
                                                : cell.active === undefined
                                                ? statusObj?.color
                                                : "#999999",
                                            border:
                                                cell.active &&
                                                cell.businessType ===
                                                    BusinessType.TARGET
                                                    ? "2px solid #0e8f48"
                                                    : defaultBorder
                                        }}
                                    >
                                        <span
                                            className={cx(
                                                "item-status",
                                                "item-length-limit"
                                            )}
                                        >
                                            {isStockTake && cell.active
                                                ? cell.address
                                                : cell?.containerDesc
                                                      ?.containerCode}
                                        </span>
                                        {cell.active === undefined && (
                                            <span
                                                className={cx(
                                                    "item-span",
                                                    "item-status"
                                                )}
                                            >
                                                {noStatusText
                                                    ? ""
                                                    : statusObj?.text}
                                            </span>
                                        )}
                                        {isFull && cell.active && (
                                            <span
                                                className={cx(
                                                    "item-span",
                                                    "item-status"
                                                )}
                                                style={{
                                                    display: "flex",
                                                    justifyContent: "flex-end"
                                                }}
                                            >
                                                待取出
                                                {/* <IntlMessages id="workstaion.outbound.text.toBeTakenOut" /> */}
                                            </span>
                                        )}
                                        <span
                                            className={cx(
                                                "item-span",
                                                "item-status",
                                                "item-container-code"
                                            )}
                                        >
                                            {cell.subContainerCode || ""}
                                        </span>
                                    </div>
                                ) : (
                                    <div
                                        key={i}
                                        className={cx(
                                            "seeding-item",
                                            breathingLamp
                                        )}
                                        onClick={(e) =>
                                            getClickStatus &&
                                            getClickStatus(cell) &&
                                            onChange &&
                                            onChange(cell)
                                        }
                                        style={{
                                            height: 100 / item.length + "%",
                                            background: backgroundColor,
                                            minHeight: 70,
                                            border: `2px solid ${borderColor}`,
                                            color: textColor
                                        }}
                                    >
                                        {cell.putWallSlotDesc &&
                                            showWallSlotDesc && (
                                                <div
                                                    className={cx(
                                                        "item-configs"
                                                    )}
                                                >
                                                    {cell.putWallSlotDesc.map(
                                                        (item, index) => {
                                                            if (
                                                                !item.propertyValue
                                                            )
                                                                return null
                                                            return (
                                                                <div
                                                                    key={
                                                                        item.propertyName
                                                                    }
                                                                    style={{
                                                                        color:
                                                                            item.color ||
                                                                            textColor ||
                                                                            "#fff",
                                                                        fontWeight:
                                                                            item.bold ||
                                                                            "100",
                                                                        fontSize: `${
                                                                            (item.textSize ||
                                                                                14) *
                                                                            scaleSize
                                                                        }px`,
                                                                        overflow:
                                                                            "hidden",
                                                                        whiteSpace:
                                                                            project ===
                                                                            "BS-Bilibili"
                                                                                ? "inherit"
                                                                                : "nowrap",
                                                                        textOverflow:
                                                                            "ellipsis",
                                                                        lineHeight: `${
                                                                            16 *
                                                                                scaleSize >
                                                                            12
                                                                                ? 16 *
                                                                                  scaleSize
                                                                                : 12
                                                                        }px`,
                                                                        width: "100%"
                                                                    }}
                                                                >
                                                                    {item.propertyValue ||
                                                                        ""}
                                                                </div>
                                                            )
                                                        }
                                                    )}
                                                    {cell.putWallSlotDesc
                                                        .length >= 2 ? null : (
                                                        <div />
                                                    )}
                                                </div>
                                            )}

                                        <div
                                            className={cx("item-code")}
                                            style={{
                                                fontSize: `${14 * scaleSize}px`
                                            }}
                                        >
                                            {cell.outboundContainerCode}
                                        </div>
                                        <div
                                            className={cx("item-status")}
                                            style={{
                                                fontSize: `${18 * scaleSize}px`
                                            }}
                                        >
                                            {text || ""}
                                        </div>
                                        <div
                                            className={cx("item-code")}
                                            style={{
                                                fontSize: `${14 * scaleSize}px`,
                                                alignItems: "end"
                                            }}
                                        >
                                            {cell.subContainerCode || ""}
                                        </div>
                                    </div>
                                )
                            })}
                    </div>
                )
            })}
        </div>
    )
}

export default SeedingWall
