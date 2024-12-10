import type { CarrierDesc } from "@/pages/wms/station/instances/outbound/type"
import { BusinessType } from "@/pages/wms/station/widgets/types"
import classNames from "classnames/bind"
import React, { memo } from "react"
import style from "../styles/Robot.module.scss"

const cx = classNames.bind(style)

// 层数64代表货叉
const FORK_LEVEL = 64

interface RobotProps {
    /** 总宽度 */
    width: number
    /** 总高度 */
    height: number
    /** 料箱选中事件 */
    onSelect?: (val: string) => void
    /** 自定义货叉样式 */
    forkStyle?: React.CSSProperties
}

/** 背篓间总间距 */
const totalSpace = 49

const Robot = (props: CarrierDesc & Partial<RobotProps>) => {
    const {
        width,
        height = 324,
        level = 6,
        onSelect,
        carrierSlots = [],
        forkStyle
    } = props
    const slotHeight = `${(height - totalSpace) / level}px`
    const fork = carrierSlots.find(
        (item) => item.level === FORK_LEVEL && item.active
    )
    const isForkActive = fork?.containerDesc?.containerCode

    const handleClick = (val: string, active?: boolean) => {
        if (val && !active && onSelect) onSelect(val)
    }

    return (
        <div className={cx("robot")} style={{ width, height }}>
            <div
                className={cx("left")}
                style={{ borderBottom: `${slotHeight} solid #f2f2f2` }}
            >
                <div
                    className={cx("slot", isForkActive ? "active-cell" : "")}
                    style={{
                        height: slotHeight,
                        marginBottom: `${totalSpace / (level - 1)}px`,
                        background: fork?.bgColor,
                        ...forkStyle
                    }}
                >
                    {
                        isForkActive ? fork.containerDesc.containerCode : "货叉"
                        // <IntlMessages id="workstaion.manualWarehousing.column.fork" />
                    }
                </div>
            </div>
            <div className={cx("right")}>
                {carrierSlots.map((item) => {
                    return item.level !== FORK_LEVEL ? (
                        <div
                            key={item.subContainerCode}
                            className={cx("slot", {
                                "active-cell": item.active,
                                "active-cell-target":
                                    item.businessType === BusinessType.TARGET
                            })}
                            onClick={() =>
                                handleClick(
                                    item?.containerDesc?.containerCode,
                                    item.active
                                )
                            }
                            style={{
                                height: slotHeight,
                                cursor: item?.containerDesc?.containerCode
                                    ? "pointer"
                                    : "not-allowed",
                                backgroundColor: item?.bgColor
                            }}
                        >
                            {!item.active ? (
                                <>
                                    {item.level}层
                                    {/* <IntlMessages id="workstaion.common.text.level" /> */}
                                </>
                            ) : (
                                item?.containerDesc?.containerCode || (
                                    <>
                                        {item.level}层
                                        {/* <IntlMessages id="workstaion.common.text.level" /> */}
                                    </>
                                )
                            )}
                        </div>
                    ) : null
                })}
            </div>
        </div>
    )
}

export default memo(Robot)
