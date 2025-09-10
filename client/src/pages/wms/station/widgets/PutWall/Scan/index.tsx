import React, { useState, forwardRef } from "react"

import { Input } from "antd"

import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { useWorkStation } from "@/pages/wms/station/state"

const Scan = (props: any, ref: any) => {
    const { onActionDispatch } = useWorkStation()

    const [scanValue, setScanValue] = useState("")

    const handlePressEnter = async (e: any) => {
        await onActionDispatch({
            eventCode: CustomActionType.INPUT,
            data: e.target.value
        })
        setScanValue("")
    }

    return (
        <Input
            className="absolute inset-0 opacity-0"
            value={scanValue}
            onChange={(e) => setScanValue(e.target.value)}
            ref={ref}
            onPressEnter={handlePressEnter}
        />
    )
}

export default forwardRef(Scan)
