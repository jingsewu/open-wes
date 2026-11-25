import React from "react"

import type {
    OperationTaskDTOS,
    PickingViewItem
} from "@/pages/wms/station/event-loop/types"
import QTYFILL from "./QTYFILL"
import SKUInfo from "./SKUInfo"
import ScanBarcode from "./ScanBarcode"

interface OperationProps {
    view?: PickingViewItem
    task?: OperationTaskDTOS
    showCount: boolean
    scanMismatched: boolean
    isBlind: boolean
}

const Operation = (props: OperationProps) => {
    const {view, task, showCount, scanMismatched, isBlind} = props
    const barcodes = (view?.skuMainDataDTO?.skuBarcode?.barcodes || []).filter(
        Boolean
    )

    return (
        <>
            <SKUInfo
                view={view}
                isBlind={isBlind}
                showBarcodes={showCount}
            />

            <div className="stocktake-stage__body">
                {showCount ? (
                    <QTYFILL
                        taskId={task?.id}
                        bookQty={task?.requiredQty ?? 0}
                        isBlind={isBlind}
                    />
                ) : (
                    <ScanBarcode
                        barcodes={barcodes}
                        mismatched={scanMismatched}
                    />
                )}
            </div>
        </>
    )
}

export default Operation
