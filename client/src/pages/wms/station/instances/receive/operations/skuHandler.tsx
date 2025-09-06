import React, {useEffect, useRef, useState} from "react"

import type {InputRef} from "antd"
import {Divider, Input, message} from "antd"
import SkuInfo from "@/pages/wms/station/widgets/common/SkuInfo"
import {useTranslation} from "react-i18next";
import request from "@/utils/requestInterceptor";

const SkuAreaHandler = (props: any) => {
    const {details, displayQty, currentSkuInfo, focusValue, onSkuChange} = props
    const inputRef = useRef<InputRef>(null)

    const [skuCode, setSkuCode] = useState<string>("")
    const {t} = useTranslation();

    useEffect(() => {
        if (focusValue !== "sku") return
        setSkuCode("")
        inputRef.current?.focus()
    }, [focusValue, details])

    const onChange = (e: any) => {
        setSkuCode(e.target.value)
    }

    const onPressEnter = () => {
        if (details) {
            const detail = details.find((item: any) => item.skuCode === skuCode)
            if (!detail) {
                setSkuCode("")
                message.warning(t("receive.station.warning.skuNotInOrder"))
                return
            }
            onSkuChange(detail)
        } else {
            request({
                method: "post",
                url: "/wms/basic/sku/getBySkuCode?skuCode=" + skuCode,
                headers: {
                    "Content-Type": "application/json"
                }
            }).then((res: any) => {
                if (!(res.status === 200 && res.data?.length > 0)) {
                    setSkuCode("")
                    message.warning(t("receive.station.warning.skuNotInOrder"))
                    return
                }
                let sku = res.data[0];
                onSkuChange({"skuId": sku.id, "skuCode": sku.skuCode, "skuName": sku.skuName})
            }).catch((error) => {
                console.log("error", error)
                setSkuCode("")
                message.warning(t("receive.station.warning.skuNotInOrder"))
            })
        }

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
            <Divider style={{margin: "12px 0"}}/>):
            {
                displayQty ?
                    (<div className="bg-gray-100 py-4 pl-6 d-flex">
                        <div>
                            <div>{t("receive.station.skuArea.receivedQty")}</div>
                            <div>/{t("receive.station.skuArea.totalQty")}</div>
                        </div>
                        <div className="border-solid border-gray-200 border-l border-r mx-4"></div>
                        <div className="text-2xl">
                            {currentSkuInfo.qtyAccepted}/{currentSkuInfo.qtyRestocked}
                        </div>
                    </div>)
                    : ""
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
