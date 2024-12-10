import React from "react"
import SKUInfo from "./SKUInfo"
import ScanBarcode from "./ScanBarcode"
import QTYFILL from "./QTYFILL"

const Operation = (props: any) => {
    const { activeTask, scanCode } = props

    return (
        <>
            <SKUInfo activeTask={activeTask} />
            {!scanCode ? (
                <ScanBarcode />
            ) : (
                <QTYFILL
                    requiredQty={activeTask?.operationTaskDTOS[0]?.requiredQty}
                    detailId={activeTask?.operationTaskDTOS[0]?.id}
                />
            )}
        </>
    )
}

export default Operation
