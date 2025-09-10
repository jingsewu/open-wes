import { Input, Button } from "antd"
import React, { useState } from "react"
import { SearchOutlined } from "@ant-design/icons"
import { useWorkStation } from "@/pages/wms/station/state"
import { CustomActionType } from "../../customActionType"

const ScanBarcode = () => {
    const { onActionDispatch } = useWorkStation()

    const [barcode, setBarcode] = useState<string>("")

    const handleChange = (e: any) => {
        setBarcode(e.target.value)
    }

    const handleFinishScan = () => {
        onActionDispatch({
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
