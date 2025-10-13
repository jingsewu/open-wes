import React, { useEffect, useRef } from "react"
import { Divider, Input } from "antd"
import type { InputRef } from "antd"
import { useTranslation } from "react-i18next"

import SkuInfo from "@/pages/wms/station/widgets/common/SkuInfo"
import type { SkuHandlerProps } from "../types"
import { useSkuScanner } from "../hooks"

const SkuAreaHandler = (props: SkuHandlerProps) => {
    const { details, displayQty, currentSkuInfo, focusValue, onSkuChange } = props
    const inputRef = useRef<InputRef>(null)
    const { t } = useTranslation()

    const { skuCode, setSkuCode, scanSku, resetSkuCode } = useSkuScanner(onSkuChange)

    useEffect(() => {
        if (focusValue !== "sku") return
        resetSkuCode()
        inputRef.current?.focus()
    }, [focusValue, details])

    const onChange = (e: any) => {
        setSkuCode(e.target.value)
    }

    const onPressEnter = () => {
        scanSku(details)
    }
    return (
        <div className="bg-white p-4 h-full">
            <div className="d-flex items-center bg-white">
                <div className="white-space-nowrap">{t("skuArea.scanBarcode")}:</div>
                <Input
                    bordered={false}
                    ref={inputRef}
                    value={skuCode}
                    onChange={onChange}
                    onPressEnter={onPressEnter}
                />
            </div>
            <Divider style={{margin: "12px 0"}}/>
            {
                displayQty ? (
                    <div className="bg-gray-100 py-4 pl-6 d-flex">
                        <div>
                            <div>{t("receive.station.skuArea.receivedQty")}</div>
                            <div>/{t("receive.station.skuArea.totalQty")}</div>
                        </div>
                        <div className="border-solid border-gray-200 border-l border-r mx-4"></div>
                        <div className="text-2xl">
                            {currentSkuInfo.qtyAccepted}/{currentSkuInfo.qtyRestocked}
                        </div>
                    </div>
                    )
                    : null
            }

            <div className="bg-gray-100 mt-4 p-3">
                <div>{t("receive.station.skuArea.skuInfo")}</div>
                <SkuInfo
                    imgWidth={160}
                    detailHeight={130}
                    skuAttributes={currentSkuInfo.batchAttributes}
                    skuName={currentSkuInfo.skuName}
                    barCode={currentSkuInfo.skuCode}
                />
            </div>
        </div>
    )
}

export default SkuAreaHandler
