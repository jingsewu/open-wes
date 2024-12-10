import { Input, Button } from "antd"
import React, { useState, useContext } from "react"
import { SearchOutlined } from "@ant-design/icons"
import { APIContext } from "@/pages/wms/station/event-loop/provider"
import { CustomActionType } from "../../customActionType"

const ScanBarcode = () => {
    const { onCustomActionDispatch } = useContext(APIContext)

    const [barcode, setBarcode] = useState<string>("")

    const handleChange = (e: any) => {
        setBarcode(e.target.value)
    }

    const handleFinishScan = () => {
        onCustomActionDispatch({
            eventCode: CustomActionType.SCAN_BARCODE,
            data: barcode
        })
    }
    return (
        <div className="pt-4">
            <Input
                size="large"
                placeholder="Scan Barcode"
                prefix={<SearchOutlined />}
                value={barcode}
                onChange={handleChange}
                onPressEnter={handleFinishScan}
            />
            <Button
                type="primary"
                block
                className="mt-2"
                onClick={handleFinishScan}
            >
                GO
            </Button>
        </div>
    )
}

export default ScanBarcode
