import React, { useState, forwardRef, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { debounce } from "lodash"

import { Col, Row, Input } from "antd"
import { ScanOutlined } from "@ant-design/icons"

const Scan = ({ inputValue, onChange, onPressEnter }: any, refs: any) => {
    const { t } = useTranslation()
    const [scanCode, setScanCode] = useState(inputValue || "")

    useEffect(() => {
        setScanCode(inputValue)
    }, [inputValue])

    const onScanInputChange = async (e: any) => {
        const value = e.target.value.trim()
        // 清空输入框时调用清空已扫SKU接口
        setScanCode(value)
        onChange && onChange(value)
    }

    const handlePressEnter = debounce(async () => {
        const res = onPressEnter && onPressEnter(scanCode)
        if ((await res) === "-1") {
            setScanCode("")
        }
    }, 500)

    return (
        <Row className="scan-input">
            <Col span={24}>
                <Input
                    size="large"
                    autoFocus
                    prefix={
                        <ScanOutlined
                            style={{
                                paddingRight: 5,
                                fontSize: 24,
                                color: "#696B7D"
                            }}
                        />
                    }
                    value={scanCode}
                    onChange={onScanInputChange}
                    onPressEnter={handlePressEnter}
                    placeholder={t("skuArea.scanOrEnterBarcode")}
                    allowClear
                    ref={refs}
                    style={{
                        height: 50,
                        borderRadius: 5,
                        background: "#F4F4F5",
                        borderColor: "#f4f4f5"
                    }}
                    data-testid="scanInboundSku"
                />
            </Col>
        </Row>
    )
}

export default forwardRef(Scan)
