import type { InputRef } from "antd"
import { Input } from "antd"
import { debounce } from "lodash"
import React, { useRef, useState } from "react"
import { useTranslation } from "react-i18next"
//
import { ProcessStatusEnum } from "@/pages/wms/station/event-loop/types"

// import type { Rule } from "@/config/JS/Project/project.types"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import type { PickerArea } from "@/pages/wms/station/instances/outbound/operations/pickingHandler"
import SkuInfo from "@/pages/wms/station/widgets/common/SkuInfo"

interface OutboundSkuInfoProps {
    isActive?: boolean
    value: PickerArea | undefined
    onChange: (value: any) => void
}

const OutboundSkuInfo = (props: OutboundSkuInfoProps) => {
    const { isActive, value, onChange } = props
    const { t } = useTranslation()
    // const { skuArea } = value || {}

    const { pickingViews = [] } = value || {}

    const scannedSkuInfo =
        pickingViews?.find(
            (item) =>
                item.operationTaskDTOS?.[0]?.taskStatus ===
                ProcessStatusEnum.PROCESSING
        ) || pickingViews?.[0]

    const toBeOperatedQty = scannedSkuInfo?.operationTaskDTOS?.reduce(
        (pre, cur) => {
            return pre + cur.toBeOperatedQty
        },
        0
    )

    let skuList: any[] = [],
        skuBatchIds: number[] = []
    pickingViews?.forEach((item) => {
        if (
            !skuBatchIds.includes(item.operationTaskDTOS?.[0]?.skuBatchStockId)
        ) {
            skuList.push({
                skuCode: item.skuMainDataDTO.skuCode,
                toBeOperatedQty: item.operationTaskDTOS?.[0]?.toBeOperatedQty,
                skuBatchStockId: item.operationTaskDTOS?.[0]?.skuBatchStockId
            })
        } else {
            const currentIndex = skuList.findIndex(
                (i) =>
                    i.skuBatchStockId ===
                    item.operationTaskDTOS?.[0]?.skuBatchStockId
            )
            skuList[currentIndex].toBeOperatedQty +=
                item.operationTaskDTOS?.[0]?.toBeOperatedQty
        }
    })

    const inputRef = useRef<InputRef>(null)
    const [state, setState] = useState("")
    const handleChange = debounce(async (e) => {
        onChange(state)
        setState("") // 清空输入框
    }, DEBOUNCE_TIME)

    if (inputRef) {
        isActive && inputRef.current?.focus()
    }
    return (
        <div className="w-full h-full d-flex flex-col ">
            <div className="d-flex pb-2" style={{ color: "#292B3E" }}>
                <span className="text-xl white-space-nowrap">
                    {t("skuArea.scanBarcode")}：
                </span>
                <Input
                    bordered={false}
                    className="w-5 text-xl"
                    data-testid="scanSkuCode"
                    value={state}
                    onChange={(e: any) => {
                        setState(e.target.value.trim())
                        inputRef.current!.focus()
                    }}
                    ref={inputRef}
                    onPressEnter={handleChange}
                />
            </div>
            <div className="d-flex w-full ">
                <div
                    className="d-flex flex-3 flex-col px-6 py-3 overflow-hidden"
                    style={{ backgroundColor: "#F8F8F8", flex: 7 }}
                >
                    <div
                        className="font-medium	text-lg pb-2 font-medium"
                        style={{ color: "#292B3E" }}
                    >
                        {t("skuArea.productDetails")}
                    </div>
                    <SkuInfo
                        // {...scannedSkuInfo}
                        skuAttributes={
                            scannedSkuInfo?.skuBatchAttributeDTO?.skuAttributes
                        }
                        skuName={scannedSkuInfo?.skuMainDataDTO.skuName}
                        barCode={
                            scannedSkuInfo?.skuMainDataDTO.skuBarcode
                                ?.barcodes[0] ||
                            scannedSkuInfo?.skuMainDataDTO.skuCode
                        }
                        url={
                            scannedSkuInfo?.skuMainDataDTO.skuAttribute
                                ?.imageUrl
                        }
                        imgWidth={160}
                        detailHeight={130}
                    />
                </div>
                <div
                    className="d-flex flex-1 flex-col items-center	justify-center text-current px-4 py-8 ml-4"
                    style={{ background: "#F8F8F8", flex: 3 }}
                >
                    <div className="text-lg pb-3">
                        {t("skuArea.numberOfPicking")}
                    </div>
                    <div
                        className="text-4xl bg-white w-full text-6xl d-flex justify-center items-center"
                        style={{ color: "#292B3E", flex: 1 }}
                    >
                        {toBeOperatedQty || 0}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default OutboundSkuInfo
