import {
    ExclamationCircleFilled,
    ScanOutlined
} from "@ant-design/icons"
import {Button, Input} from "antd"
import type {InputRef} from "antd"
import React, {useEffect, useRef, useState} from "react"
import {useTranslation} from "react-i18next"

import {useWorkStation} from "@/pages/wms/station/state"
import {CustomActionType} from "../../customActionType"

interface ScanBarcodeProps {
    barcodes: string[]
    mismatched: boolean
}

const ScanBarcode = (props: ScanBarcodeProps) => {
    const {barcodes, mismatched} = props
    const {t} = useTranslation()
    const {onActionDispatch} = useWorkStation()

    const [barcode, setBarcode] = useState("")
    const inputRef = useRef<InputRef>(null)

    useEffect(() => {
        inputRef.current?.focus()
        if (mismatched) {
            setBarcode("")
        }
    }, [mismatched])

    const handleFinishScan = async () => {
        const code = barcode.trim()
        if (!code) return
        // Clear before dispatch: a rejected scan must leave an empty field so
        // the next hardware scan does not append to the previous code.
        setBarcode("")
        await onActionDispatch({
            eventCode: CustomActionType.SCAN_BARCODE,
            data: code
        })
    }

    return (
        <div className="stocktake-scan">
            <h2 className="stocktake-scan__title">
                {t("stocktake.station.scan.title")}
            </h2>
            <p className="stocktake-scan__hint">
                {t("stocktake.station.scan.hint")}
            </p>

            <div className="stocktake-scan__field">
                <Input
                    ref={inputRef}
                    autoFocus
                    size="large"
                    data-testid="stocktake-scan-input"
                    aria-label={t("stocktake.station.scan.title")}
                    placeholder={t("stocktake.station.scan.placeholder")}
                    prefix={<ScanOutlined aria-hidden="true" />}
                    value={barcode}
                    onChange={(event) => setBarcode(event.target.value)}
                    onPressEnter={handleFinishScan}
                />
                <Button
                    type="primary"
                    className="stocktake-scan__go"
                    disabled={!barcode.trim()}
                    onClick={handleFinishScan}
                >
                    {t("stocktake.station.scan.action")}
                </Button>
            </div>

            {barcodes.length > 0 && (
                <div className="stocktake-scan__expected">
                    <span className="stocktake-field-label">
                        {t("stocktake.station.scan.expected")}
                    </span>
                    {barcodes.map((code) => (
                        <span
                            key={code}
                            className="stocktake-chip stocktake-mono"
                        >
                            {code}
                        </span>
                    ))}
                </div>
            )}

            {mismatched && (
                <div className="stocktake-notice" role="alert">
                    <ExclamationCircleFilled aria-hidden="true" />
                    <span>{t("stocktake.station.scan.mismatch")}</span>
                </div>
            )}
        </div>
    )
}

export default ScanBarcode
