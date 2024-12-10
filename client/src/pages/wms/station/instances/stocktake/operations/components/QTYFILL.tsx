import React, { useState, useContext } from "react"
import { useTranslation } from "react-i18next"
import { Button, Typography, Divider, InputNumber, Modal, message } from "antd"
import { CheckOutlined, MinusOutlined, PlusOutlined } from "@ant-design/icons"
import { APIContext } from "@/pages/wms/station/event-loop/provider"
import { CustomActionType } from "../../customActionType"
import { MessageType } from "@/pages/wms/station/widgets/message"

const { Title, Text } = Typography

const QTYFILL = (props: any) => {
    const { requiredQty, detailId } = props
    const { onCustomActionDispatch } = useContext(APIContext)
    const { t } = useTranslation()

    const [inputQty, setInputQty] = useState<number>(requiredQty)

    const onChange = (value: number) => {
        setInputQty(value)
    }

    const handleMinus = () => {
        setInputQty((prev) => prev - 1)
    }

    const handlePlus = () => {
        setInputQty((prev) => prev + 1)
    }

    const onSubmit = async () => {
        if (!inputQty && inputQty !== 0) {
            message.warning({
                content: t("toast.incorrectQty")
            })
            return
        }
        if (inputQty !== requiredQty) {
            Modal.confirm({
                title: t("toast.inconsistentQuantity"),
                content: t("toast.inconsistentToastContent"),
                onOk: () => {
                    stockTakeSubmit()
                }
            })
            return
        }
        stockTakeSubmit()
    }

    const stockTakeSubmit = async () => {
        const { code, msg } = await onCustomActionDispatch({
            eventCode: CustomActionType.STOCKTAKE_SUBMIT,
            data: {
                detailId,
                stocktakeQty: inputQty
            }
        })

        if (code !== "-1") {
            setInputQty(0)
        }
    }
    return (
        <div className="text-center">
            <Divider />
            <Title level={4}>QTY ABLE TO FILL </Title>
            <div className="d-flex justify-center items-center pt-2">
                <Button
                    shape="circle"
                    size="large"
                    icon={<MinusOutlined />}
                    disabled={inputQty <= 0}
                    onClick={handleMinus}
                />
                <InputNumber
                    size="large"
                    className="mx-3 input-text-center"
                    controls={false}
                    style={{
                        width: 180,
                        fontSize: 24
                    }}
                    min={0}
                    max={requiredQty}
                    defaultValue={requiredQty}
                    value={inputQty}
                    onChange={onChange}
                />
                <Button
                    shape="circle"
                    size="large"
                    icon={<PlusOutlined />}
                    disabled={inputQty >= requiredQty}
                    onClick={handlePlus}
                />
            </div>
            <Button
                size="large"
                type="primary"
                block
                className="my-4"
                style={{ backgroundColor: "#fa7317", borderColor: "#fa7317" }}
                onClick={onSubmit}
            >
                <span>SUBMIT</span>
                <CheckOutlined />
            </Button>
        </div>
    )
}

export default QTYFILL
