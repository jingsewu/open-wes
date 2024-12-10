import { Badge } from "antd"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import React, { useContext } from "react"
import { APIContext } from "@/pages/wms/station/event-loop/provider"

import Icon from "@ant-design/icons"
import { putWallViewsItem } from "@/pages/wms/station/event-loop/types"
import WallLeftSvg from "@/icon/wall/wall_left.svg"
import WallRightSvg from "@/icon/wall/wall_right.svg"

const locationEnum = {
    LEFT: <WallLeftSvg />,
    RIGHT: <WallRightSvg />
}

const MergeIcon = (props: { putWallViews: putWallViewsItem[] }) => {
    const { putWallViews = [] } = props
    const { onCustomActionDispatch } = useContext(APIContext)

    const onLocationChange = async (params: "LEFT" | "RIGHT") => {
        await onCustomActionDispatch({
            eventCode: CustomActionType.CHOOSE_PUT_WALL,
            data: params
        })
    }

    return (
        <>
            {putWallViews.map((item, index) => (
                <span key={index} style={{ marginRight: "16px" }}>
                    <Badge dot={item.showHasTask}>
                        <Icon
                            onClick={(e) => {
                                e.stopPropagation()
                                onLocationChange?.(item.location)
                            }}
                            component={() => locationEnum[item.location]}
                            className={`text-2xl text-${
                                item.active ? "primary" : "secondary"
                            }`}
                        />
                    </Badge>
                </span>
            ))}
        </>
    )
}

export default MergeIcon
