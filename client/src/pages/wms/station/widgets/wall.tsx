import Icon from "@ant-design/icons"
import type { InputRef } from "antd"
import { Button, Input, Space } from "antd"
import classNames from "classnames/bind"
import React, { useEffect, useRef, useState } from "react"

import { ReactComponent as WallAllSvg } from "@/icon/wall/wall_all.svg"
import { ReactComponent as WallLeftSvg } from "@/icon/wall/wall_left.svg"
import { ReactComponent as WallRightSvg } from "@/icon/wall/wall_right.svg"
// import IntlMessages from "@/util/IntlMessages"

import SeedingWall from "./seedingWall"
import style from "./styles.module.scss"

const cx = classNames.bind(style)
const wall = {
    LEFT: "播种墙（左）",
    // < IntlMessages id = "workstaion.outbound.text.seedingWall(left)" />, // '播种墙(左)'
    RIGHT: "播种墙（右）"
    // <IntlMessages id="workstaion.outbound.text.seedingWall(right)" /> // '播种墙(右)'
}
export interface SeedingProps {
    skuInfo: string
    isShelves: boolean // 是缓存货架 or 播种墙
}

export interface SeedingConfirmProps {
    skuCode: string
}

export interface PutWallViews {
    level: number
    bay: number
    active?: boolean
    location: string
    putWallCode: string
    putWallSlots: PutWallSlotView[]
}
export interface PutWallSlotView {
    address: string
    slotStatus: any
    containerCode: any
    propertyValue: string
    level: number
    bay: number
    subContainerCode: string
    status: string
    dispatchQty?: number
    active?: boolean
    containerDesc?: interfaceDesc
    outboundContainerCode?: string
    enable: boolean
    allowSplit?: boolean
    putWallSlotDesc?: PutWallSlotDesc[]
    /** 区分是来源箱还是目标箱 */
    businessType?: string
    backgroundColor: string
    borderColor: string
    text: string
    textColor: string
    breathingLamp: string
    amountDisplayRule: any
}

export interface interfaceDesc {
    containerCode: string
}
export interface PutWallSlotDesc {
    textSize: number
    bold: string
    color: any
    propertyName: string
    propertyValue: string
}

export interface Iprops {
    value: PutWallViews[]
    isShelves?: boolean
    onCustomActionDispatch?: (value: any) => Promise<void>
}

const WallTitle = (props: {
    location: "LEFT" | "RIGHT"
    active: boolean
    containerCarrier: "SHELF"
    showHasTask: boolean
    onTitleClick?: (param: "LEFT" | "RIGHT") => void
}) => {
    const {
        location,
        active,
        onTitleClick,
        containerCarrier,
        showHasTask = false
    } = props
    return (
        <Button
            type="text"
            onClick={() =>
                containerCarrier !== "SHELF" &&
                onTitleClick &&
                onTitleClick(location)
            }
            style={{ color: active ? "#004CAA" : "#666" }}
        >
            {showHasTask && (
                <span
                    style={{
                        position: "absolute",
                        top: "0",
                        right: "6px",
                        display: "block",
                        width: "6px",
                        backgroundColor: "#F5222D",
                        height: "6px",
                        borderRadius: "50%"
                    }}
                />
            )}
            {wall[location] || location}
        </Button>
    )
}

const Wall = (props: any) => {
    const inputRef = useRef<InputRef>(null)
    const {
        value = {},
        isShelves = false,
        onPressEnter,
        onChange,
        isActive,
        isFull,
        onTitleClick,
        isStockTake,
        statusColorList,
        getClickStatus
    } = props
    const { putWallViews = [], putWallDisplayStyle, noStatusText } = value
    const [locationType, setLocaltion] = useState("ALL")
    const [putWallViewsList, setPutWallViewsList] = useState(
        locationType === "ALL"
            ? putWallViews
            : putWallViews.filter((item: any) => item.location === locationType)
    )
    const [putwallValue, setPutwallValue] = useState("")

    useEffect(() => {
        if (putWallViews && putWallViews.length > 0) {
            setPutWallViewsList(
                locationType === "ALL"
                    ? putWallViews
                    : putWallViews.filter(
                          (item: any) => item.location === locationType
                      )
            )
        }
    }, [putWallViews, locationType])

    const handlePressEnter = async (e: any) => {
        await onPressEnter(e.target.value)
        setPutwallValue("")
    }

    const sharedProps = {
        style: {
            width: 0,
            height: 0,
            border: "none",
            padding: 0,
            boxShadow: "none"
        },
        defaultValue: "",
        value: putwallValue
    }

    const onScanChange = (e: any) => {
        setPutwallValue(e.target.value)
    }

    if (isActive) {
        inputRef.current?.focus()
    }

    const handleInputFocus = () => {
        // 利用冒泡解决点击播种墙后隐藏input失焦问题
        inputRef.current?.focus()
    }
    const iconChange = (type: "ALL" | "LEFT" | "RIGHT") => {
        setLocaltion(type)
    }
    return (
        <>
            <div style={{ position: "absolute" }}>
                <Input
                    ref={inputRef}
                    {...sharedProps}
                    onChange={onScanChange}
                    onPressEnter={handlePressEnter}
                />
            </div>
            {putWallDisplayStyle !== "SPLIT" &&
                putWallViewsList.length > 0 &&
                putWallViewsList[0]?.containerCarrier !== "SHELF" && (
                    <div className={cx("seeding-top")}>
                        <div>
                            <Icon
                                onClick={() => iconChange("ALL")}
                                component={WallAllSvg}
                                style={{
                                    fontSize: "32px",
                                    color:
                                        locationType === "ALL"
                                            ? "#004CAA"
                                            : "#999999"
                                }}
                            />
                            {putWallViews.length > 1 && (
                                <>
                                    <Icon
                                        onClick={() => iconChange("LEFT")}
                                        component={WallLeftSvg}
                                        style={{
                                            fontSize: "32px",
                                            color:
                                                locationType === "LEFT"
                                                    ? "#004CAA"
                                                    : "#999999"
                                        }}
                                    />
                                    <Icon
                                        onClick={() => iconChange("RIGHT")}
                                        component={WallRightSvg}
                                        style={{
                                            fontSize: "32px",
                                            color:
                                                locationType === "RIGHT"
                                                    ? "#004CAA"
                                                    : "#999999"
                                        }}
                                    />
                                </>
                            )}
                        </div>
                        <div className={cx("seeding-model")}>
                            {statusColorList.map((item: any, index: number) => (
                                <div key={index}>
                                    <span
                                        className={cx(item.breathingLamp)}
                                        style={{
                                            backgroundColor:
                                                item?.color?.backgroundColor,
                                            border: `1px solid ${item?.color?.borderColor}`
                                        }}
                                    />
                                    {item.text}
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            <Space
                className={cx("seeding-container")}
                onClick={handleInputFocus}
                size={32}
            >
                {putWallViewsList.map((item: any, index: number) => {
                    const deliverItem = item.carrierDesc
                        ? item.carrierDesc
                        : item

                    return item.active ? (
                        <div key={index} className={cx("small-container")}>
                            <h3
                                color="primary"
                                style={{
                                    textAlign: "center",
                                    margin: 0,
                                    marginBottom: 16
                                }}
                            >
                                {putWallDisplayStyle === "SPLIT" &&
                                item.containerCarrier !== "SHELF"
                                    ? putWallViews.map((k: any) => {
                                          return (
                                              <WallTitle
                                                  key={k.location}
                                                  showHasTask={k.showHasTask}
                                                  containerCarrier={
                                                      item.containerCarrier
                                                  }
                                                  location={
                                                      k.containerCode ||
                                                      k.location ||
                                                      k.groupCode
                                                  }
                                                  active={k.active}
                                                  onTitleClick={onTitleClick}
                                              />
                                          )
                                      })
                                    : null}
                                {item.containerCarrier === "SHELF" &&
                                    (item.containerCode ||
                                        item.location ||
                                        item.groupCode)}
                            </h3>

                            <SeedingWall
                                {...deliverItem}
                                putWallViewsLength={putWallViewsList.length}
                                noStatusText={noStatusText}
                                isFull={isFull}
                                putWallSlotView={
                                    item.putWallSlotView ||
                                    item.carrierDesc.carrierSlots
                                }
                                isShelves={isShelves}
                                isStockTake={isStockTake}
                                onChange={onChange}
                                getClickStatus={getClickStatus}
                            />
                        </div>
                    ) : null
                })}
            </Space>
        </>
    )
}

export default Wall
