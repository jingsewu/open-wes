import React, { useState, useEffect } from "react"

import { Descriptions, Modal, Button, Row, Col } from "antd"
import ModalContent from "./ModalContent"
import request from "@/utils/requestInterceptor"
import { CustomActionType } from "@/pages/wms/station/instances/stocktake/customActionType"

interface Option {
    label: string
    value: string
}

const LocationInfo = (props: any) => {
    const {
        containerCode,
        containerId,
        face,
        containerAttributes,
        onCustomActionDispatch
    } = props
    // const [isModalOpen, setIsModalOpen] = useState(false)
    const [modalConfig, setModalConfig] = useState({
        isOpen: false,
        key: ""
    })
    const [locationSize, setLocationSize] = useState<string>("")
    const [count, setCount] = useState<number | null>()

    useEffect(() => {
        getStockTakeOptions()
    }, [containerAttributes.subSpecCode])

    const getStockTakeOptions = async () => {
        const res: any = await request({
            method: "get",
            url: `post:/mdm/config/dictionary/getAll`
        })
        // setSpecSlotOptions(res?.data?.CallContainerSlotSpecCode || [])
        const size = res?.data?.CallContainerSlotSpecCode?.find(
            (item: any) => item.value === containerAttributes.subSpecCode
        )?.label
        setLocationSize(size)
    }

    const handleQTYClick = (key: string) => {
        setModalConfig({ isOpen: true, key })
    }

    const onCountChange = (value: number | null) => {
        setCount(value)
    }

    const handleCancel = () => {
        setCount(null)
        setModalConfig({
            isOpen: false,
            key: ""
        })
    }

    const handleNext = async () => {
        const { code, msg } = await onCustomActionDispatch({
            eventCode: CustomActionType.RELOCATION_ABNORMAL,
            data: {
                qtyInput: count,
                containerId,
                containerLocationType: containerAttributes.locationCode,
                operationTaskId: containerAttributes.operationTaskId,
                stockIdStr: containerAttributes.stockIds,
                reason: ""
            }
        })
        if (code !== "-1") {
            handleCancel()
        }
    }

    return (
        <div
            className="w-full description_box"
            style={{ padding: "10px 10px 0" }}
        >
            <Descriptions
                // title="Responsive Descriptions"
                bordered={true}
                size="small"
                column={{ sm: 1 }}
                labelStyle={{
                    textAlign: "right",
                    width: 180,
                    padding: "3px 10px"
                }}
                contentStyle={{
                    // backgroundColor: "#ffff00",
                    fontWeight: "bold",
                    padding: "3px 10px",
                    backgroundColor: "#d9d9d9"
                }}
                style={{ border: "1px solid #000", borderBottom: 0 }}
            >
                <Descriptions.Item label="Container Code">
                    {containerCode}
                </Descriptions.Item>
                <Descriptions.Item label="Container Face">
                    {face}
                </Descriptions.Item>
                <Descriptions.Item
                    label="Location QTY"
                    className="description_value"
                >
                    <span
                        className="block"
                        onClick={() => handleQTYClick("locationQTY")}
                    >
                        {containerAttributes.skuTotalQty}
                    </span>
                </Descriptions.Item>
                <Descriptions.Item label="Location Size">
                    {locationSize}
                </Descriptions.Item>
                <Descriptions.Item
                    label="Max Capacity"
                    className="description_value"
                >
                    <span
                        className="block"
                        onClick={() => handleQTYClick("maxQTY")}
                    >
                        {containerAttributes.skuMaxQty}
                    </span>
                </Descriptions.Item>
            </Descriptions>
            <Modal
                title=""
                centered={true}
                open={modalConfig.isOpen}
                closable={false}
                footer={
                    <div className="d-flex justify-between">
                        <Button
                            size="large"
                            // shape="round"
                            onClick={handleCancel}
                            style={{
                                backgroundColor: "red",
                                color: "#fff",
                                width: "40%"
                            }}
                        >
                            Cancel
                        </Button>
                        <Button
                            size="large"
                            // shape="round"
                            onClick={handleNext}
                            style={{
                                backgroundColor: "#47d359",
                                color: "#fff",
                                width: "40%"
                            }}
                        >
                            Next
                        </Button>
                    </div>
                }
                width={1000}
            >
                <ModalContent
                    keyField={modalConfig.key}
                    containerAttributes={containerAttributes}
                    locationSize={locationSize}
                    onCountChange={onCountChange}
                />
            </Modal>
        </div>
    )
}

export default LocationInfo
