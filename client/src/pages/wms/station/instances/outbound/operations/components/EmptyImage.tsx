import React from "react"
import { Typography } from "antd"

import EmptyImg from "@/icon/default_empty_image.png"

import ScanContainer from "./ScanContainer"
import { WorkLocationViews } from "@/pages/wms/station/event-loop/types"
import { useWorkStation } from "@/pages/wms/station/state"

const { Title } = Typography

// 定义 EmptyImage 组件的 props 类型
interface EmptyImageProps {
    /** 扫码完成事件 */
    onChange?: (param: string | undefined) => void
    /** 点击扫码框展示input事件 */
    handleShowInput?: () => void
}

const EmptyImage: React.FC<EmptyImageProps> = ({
    onChange,
    handleShowInput
}) => {
    const { workStationEvent } = useWorkStation()

    const workLocationArea = workStationEvent?.workLocationArea
    const workLocationViews = workLocationArea?.workLocationViews || []

    // 检查是否包含传送带工作站
    const isIncludeConveyorStation = React.useMemo(() => {
        if (workLocationViews.length <= 1) return false

        const enabledView = workLocationViews.find(
            (item: WorkLocationViews) => item.enable
        )
        return enabledView?.workLocationType?.includes("CONVEYOR") ?? false
    }, [workLocationViews])
    return (
        <>
            <img src={EmptyImg} alt="空状态图片" style={{ height: 200 }} />
            <Title level={4}></Title>
            {isIncludeConveyorStation && (
                <ScanContainer
                    handleShowInput={handleShowInput}
                    onChange={onChange}
                    isDefaultPage={true}
                />
            )}
        </>
    )
}

export default EmptyImage
