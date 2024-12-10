import { useTranslation } from "react-i18next"

import PutWall from "@/pages/wms/station/widgets/PutWall"
import React, { useImperativeHandle, useState } from "react"
import classNames from "classnames/bind"
import { onSaveRequest } from "./reqeust"
import type { putWallViewsItem } from "@/pages/wms/station/event-loop/types"
import { viewsDataAdaptor, changeStateAdaptor } from "./utils"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import warning from "@/icon/warning.png"
import { putWallStatusTextMap } from "@/pages/wms/station/instances/outbound/custom-actions/SplitContainer/SplitContent"

const BatchMultiPutWall = (props: any) => {
    const { operationsMap, onCustomActionDispatch, message, refs } = props
    const { t } = useTranslation()
    const putWallArea = operationsMap.get(ChooseArea.putWallArea)

    const { putWallDisplayStyle, putWallViews, putWallTagConfigDTO } =
        putWallArea
    // 更改槽位状态为可选(OPTIONAL)或者禁用(DISABLED)
    const initViewsData = viewsDataAdaptor(putWallViews)

    const [viewsData, setViewsData] = useState(initViewsData)

    // 设计要求，暴露当前的方法
    useImperativeHandle(refs, () => ({
        onSave
    }))

    /**
     * 多选槽口，更新页面状态
     * @param value
     */
    const onSlotClick = (current: any) => {
        if (current.putWallSlotStatus === "DISABLE") return
        let result = changeStateAdaptor(
            viewsData as putWallViewsItem[],
            current
        )
        setViewsData(result) // 更新本地state
    }

    /**
     * 点击左上角布局按钮，更新页面状态
     * @param location
     */
    const onLocationChange = (location: "LEFT" | "RIGHT") => {
        setViewsData(
            viewsData.map((item) => {
                return {
                    ...item,
                    active: item.location === location
                }
            })
        )
    }

    /**
     * 点击保存，保存数据到server
     */
    const onSave = async () => {
        return await onSaveRequest(viewsData as putWallViewsItem[], {
            onCustomActionDispatch,
            message
        })
    }

    return (
        <div className="d-flex flex-col" style={{ height: "50vh" }}>
            <div className="text-center">
                <img className="w-18 h-16" src={warning} alt="alert icon" />
                <span className="block my-3 text-base font-bold text-center">
                    {t("modal.clickSlotToBeUnlocked")}
                    {/* <IntlMessages id="workstaion.outbound.text.clickToUnbindBox" /> */}
                </span>
            </div>
            <PutWall
                onSlotClick={onSlotClick}
                putWallDisplayStyle={putWallDisplayStyle}
                putWallViews={viewsData as putWallViewsItem[]}
                onLocationChange={onLocationChange}
                putWallStatusTextMap={putWallStatusTextMap}
                putWallTagConfigDTO={putWallTagConfigDTO}
            />
        </div>
    )
}

export default BatchMultiPutWall
