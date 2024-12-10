import { useDebounceFn } from "ahooks"
import { Col, Row } from "antd"
import classNames from "classnames/bind"
import React from "react"

import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import type {
    ContainerDesc,
    SubContainer
} from "@/pages/wms/station/instances/outbound/type"
import { ShelvesStatus } from "@/pages/wms/station/widgets/seedingWall"
import { BusinessType } from "@/pages/wms/station/widgets/types"

import style from "../styles/container.module.scss"
const cx = classNames.bind(style)

export type Angle = 0 | 90 | 180 | 270

interface ContainerProps {
    /** 料箱总宽度 */
    width?: number | string
    /** 料箱总高度 */
    height?: number
    /** 料箱格口是否可点击选中 */
    isOptional?: boolean
    /** 当前选择的值 */
    value?: string
    /** 格口点击事件 */
    onClick?: (param: string, name?: string) => void
}

// 根据料箱旋转角度渲染料箱格口
const useRotationAngle = ({
    rotationAngle,
    level,
    bay,
    subContainers = [],
    width,
    height
}: Partial<ContainerDesc & ContainerProps>) => {
    const isVertical = rotationAngle === 90 || rotationAngle === 270

    const verticalAttr = {
        level: "bay",
        bay: "level",
        subContainerLevel: "subContainerBay",
        subContainerBay: "subContainerLevel",
        width: height,
        height: width,
        rowsNum: bay
    }

    const defaultAttr = {
        level: "level",
        subContainerLevel: "subContainerLevel",
        subContainerBay: "subContainerBay",
        bay: "bay",
        width,
        height,
        rowsNum: level
    }
    const attr = isVertical ? verticalAttr : defaultAttr

    let renderAttr = {
        rowReverse: rotationAngle === 0 || rotationAngle === 270,
        colReverse: rotationAngle === 180 || rotationAngle === 270,
        ...attr
    }

    const rows = [...new Array(renderAttr.rowsNum)].map((r) => r + 1)
    const rotateRows = renderAttr.rowReverse ? rows.reverse() : rows
    const containerSlotsList = rotateRows.map((row) => {
        return subContainers
            .filter(
                (c) =>
                    (c[renderAttr.level as keyof SubContainer] ||
                        c[
                            renderAttr.subContainerLevel as keyof SubContainer
                        ]) === row
            )
            .sort((c, c1) => {
                const bayC1 =
                    c1[renderAttr.bay as keyof SubContainer] ||
                    c1[renderAttr.subContainerBay as keyof SubContainer]
                const bayC =
                    c[renderAttr.bay as keyof SubContainer] ||
                    c[renderAttr.subContainerBay as keyof SubContainer]
                return renderAttr.colReverse ? bayC1 - bayC : bayC - bayC1
            })
    })

    return {
        containerSlotsList,
        rotateWidth: renderAttr.width,
        rotateHeight: renderAttr.height,
        rowsNum: renderAttr.rowsNum
    }
}

const Container = (props: Partial<ContainerDesc & ContainerProps>) => {
    const {
        width = 224,
        height = 256,
        rotationAngle = 0,
        level = 3,
        bay = 2,
        value,
        subContainers,
        isOptional = false,
        onClick
    } = props

    const {
        containerSlotsList,
        rotateWidth,
        rotateHeight,
        rowsNum = 1
    } = useRotationAngle({
        rotationAngle,
        level,
        bay,
        subContainers,
        width,
        height
    })
    const handleClick = useDebounceFn(
        (subContainerCode, subContainerName) => {
            onClick && onClick(subContainerCode, subContainerName)
        },
        { wait: DEBOUNCE_TIME, leading: true }
    ).run

    return (
        <div
            className={cx("container")}
            style={{
                width: rotateWidth,
                height: rotateHeight
            }}
        >
            {containerSlotsList.map((row, idx) => {
                return (
                    <Row key={idx} style={{ height: `${100 / rowsNum}%` }}>
                        {row.map((item) => {
                            return (
                                <Col key={item.subContainerCode} flex="auto">
                                    <div
                                        className={cx("container-slot", {
                                            optionslot: isOptional,
                                            active: value
                                                ? value ===
                                                  item.subContainerCode
                                                : item.active,
                                            "active-source":
                                                item.active &&
                                                item.businessType ===
                                                    BusinessType.SOURCE,
                                            "active-target":
                                                item.active &&
                                                item.businessType ===
                                                    BusinessType.TARGET
                                        })}
                                        onClick={() =>
                                            handleClick(
                                                item.subContainerCode,
                                                item.subContainerName
                                            )
                                        }
                                        style={{
                                            backgroundColor: item.slotStatus
                                                ? ShelvesStatus[
                                                      item.slotStatus as keyof typeof ShelvesStatus
                                                  ].bgColor
                                                : "",
                                            border: `2px solid ${
                                                item.slotStatus
                                                    ? ShelvesStatus[
                                                          item.slotStatus as keyof typeof ShelvesStatus
                                                      ].borderColor
                                                    : ""
                                            }`,
                                            color: item.slotStatus
                                                ? ShelvesStatus[
                                                      item.slotStatus as keyof typeof ShelvesStatus
                                                  ].color
                                                : ""
                                        }}
                                    >
                                        {isOptional || item.active
                                            ? item.subContainerName ||
                                              item.subContainerCode
                                            : null}
                                    </div>
                                </Col>
                            )
                        })}
                    </Row>
                )
            })}
        </div>
    )
}

export default Container
