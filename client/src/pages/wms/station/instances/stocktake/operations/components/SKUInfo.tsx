import {EyeInvisibleOutlined, PictureOutlined} from "@ant-design/icons"
import React, {useEffect, useState} from "react"
import {useTranslation} from "react-i18next"

import type {PickingViewItem} from "@/pages/wms/station/event-loop/types"

interface SKUInfoProps {
    view?: PickingViewItem
    isBlind: boolean
    showBarcodes: boolean
}

const MAX_VISIBLE_BARCODES = 2

const SKUInfo = (props: SKUInfoProps) => {
    const {view, isBlind, showBarcodes} = props
    const {t} = useTranslation()

    const sku = view?.skuMainDataDTO
    const task = view?.operationTaskDTOs?.[0]
    const barcodes = Array.from(
        new Set((sku?.skuBarcode?.barcodes || []).filter(Boolean))
    )
    const imageUrl = sku?.skuAttribute?.imageUrl
    const [imageFailed, setImageFailed] = useState(false)

    useEffect(() => {
        setImageFailed(false)
    }, [imageUrl])

    const visibleBarcodes = barcodes.slice(0, MAX_VISIBLE_BARCODES)
    const hiddenBarcodeCount = barcodes.length - visibleBarcodes.length

    return (
        <header className="stocktake-product">
            <div className="stocktake-product__media">
                {imageUrl && !imageFailed ? (
                    <img
                        src={imageUrl}
                        alt={sku?.skuName || ""}
                        onError={() => setImageFailed(true)}
                    />
                ) : (
                    <PictureOutlined aria-hidden="true" />
                )}
            </div>

            <div className="stocktake-product__body">
                <div className="stocktake-product__code stocktake-mono">
                    SKU {sku?.skuCode || "—"}
                </div>
                <h2
                    className="stocktake-product__name"
                    title={sku?.skuName || undefined}
                >
                    {sku?.skuName || t("stocktake.station.product.unknown")}
                </h2>
                <div className="stocktake-product__meta">
                    {showBarcodes &&
                        visibleBarcodes.map((barcode) => (
                            <span
                                key={barcode}
                                className="stocktake-chip stocktake-mono"
                            >
                                {barcode}
                            </span>
                        ))}
                    {showBarcodes && hiddenBarcodeCount > 0 && (
                        <span className="stocktake-chip stocktake-mono">
                            {t("stocktake.station.product.moreBarcodes", {
                                count: hiddenBarcodeCount
                            })}
                        </span>
                    )}
                    {isBlind && (
                        <span className="stocktake-chip stocktake-chip--blind">
                            <EyeInvisibleOutlined aria-hidden="true" />
                            {t("stocktake.station.method.blind")}
                        </span>
                    )}
                </div>
            </div>

            {!isBlind && (
                <div className="stocktake-system">
                    <span className="stocktake-system__label">
                        {t("stocktake.station.product.systemQty")}
                    </span>
                    <span className="stocktake-system__value stocktake-mono">
                        {task?.requiredQty ?? 0}
                    </span>
                </div>
            )}
        </header>
    )
}

export default SKUInfo
