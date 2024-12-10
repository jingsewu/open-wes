/**
 * 绑箱提示
 */
import { TipType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
import { Button, Result } from "antd"
import React from "react"

export default function ErrorRemindHandler(props: any) {
    const { value } = props
    const { errorInfos = [], onCustomActionDispatch, buttonText } = value

    const title = errorInfos.find((item: any) => item.bold).errorDesc
    const subTitle = errorInfos.find((item: any) => !item.bold).errorDesc

    const handleConfirm = () => {
        onCustomActionDispatch({
            eventCode: "CLOSE_TIP",
            data: {
                tipType: TipType.SCAN_ERROR_REMIND_TIP
            }
        })
    }

    return (
        <Result
            status="warning"
            title={title}
            subTitle={subTitle}
            extra={
                <Button type="primary" size="large" onClick={handleConfirm}>
                    {buttonText}
                </Button>
            }
        />
    )
}
