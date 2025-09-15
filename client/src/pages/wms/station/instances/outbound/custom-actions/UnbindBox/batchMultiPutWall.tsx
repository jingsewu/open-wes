import { useTranslation } from "react-i18next"

import PutWall from "@/pages/wms/station/widgets/PutWall"
import React, { useImperativeHandle, useState, useCallback } from "react"
import { onSaveRequest } from "./reqeust"
import type { PutWallViewsItem } from "@/pages/wms/station/event-loop/types"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import { changeStateAdaptor, viewsDataAdaptor } from "./utils"
import warning from "@/icon/warning.png"
import { putWallStatusTextMap } from "@/pages/wms/station/instances/outbound/custom-actions/SplitContainer/SplitContent"
import { useWorkStation } from "@/pages/wms/station/state"

const BatchMultiPutWall = (props: any) => {
    const { operationsMap, refs } = props
    const { t } = useTranslation()
    const { store, onActionDispatch, message } = useWorkStation()

    const putWallArea =
        operationsMap.get(ChooseArea.putWallArea) ||
        store.workStationEvent?.putWallArea

    const { putWallDisplayStyle, putWallViews, putWallTagConfigDTO } =
        putWallArea || {}

    const [viewsData, setViewsData] = useState(() =>
        viewsDataAdaptor(putWallViews || [])
    )

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
            viewsData as PutWallViewsItem[],
            current
        )
        setViewsData(result) // 更新本地state
    }

    /**
     * 点击左上角布局按钮，更新页面状态
     * @param location
     */
    const onLocationChange = (location: "LEFT" | "RIGHT") => {
        setViewsData((prevData) =>
            prevData.map((item) => ({
                ...item,
                active: item.location === location
            }))
        )
    }

    /**
     * 点击保存，保存数据到server
     */
    const onSave = useCallback(async () => {
        return await onSaveRequest(viewsData as PutWallViewsItem[], {
            onActionDispatch,
            message
        })
    }, [viewsData])

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
                putWallViews={viewsData as PutWallViewsItem[]}
                onLocationChange={onLocationChange}
                putWallStatusTextMap={putWallStatusTextMap}
                putWallTagConfigDTO={putWallTagConfigDTO}
            />
        </div>
    )
}

export default BatchMultiPutWall
