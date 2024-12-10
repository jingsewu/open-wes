import React, { useRef } from "react"
import type { InputRef } from "antd"

import { Row, Col } from "antd"
import Count from "@/pages/wms/station/widgets/common/Count"

const Config = {
    locationQTY: {
        leftTitle: "SYSTEM HAS",
        rightTitle: "YOU FOUND"
    },
    maxQTY: {
        leftTitle: "Current Maximum Capacity",
        rightTitle: "Your Determined Maximum Capacity"
    }
}

const Box = (props: any) => {
    const { keyField = "locationQTY", quantity, onCountChange } = props
    const countRef = useRef<InputRef>(null)
    return (
        <Row justify="space-between">
            <Col
                span={10}
                className="d-flex flex-col"
                style={{ border: "1px solid #000" }}
            >
                <div className="text-xl font-semibold text-center py-2">
                    {Config[keyField].leftTitle}
                </div>
                <div
                    className="flex-1 text-2xl  font-bold d-flex justify-center items-center"
                    style={{ backgroundColor: "#dae9f8" }}
                >
                    {quantity}
                </div>
            </Col>
            <Col span={13} style={{ border: "1px solid #000" }}>
                <div
                    className="text-xl font-semibold text-center py-2"
                    style={{ backgroundColor: "#f7c7ac" }}
                >
                    {Config[keyField].rightTitle}
                </div>
                <Count
                    ref={countRef}
                    height={160}
                    width="100%"
                    wantScant={false}
                    isWorkStationInput
                    onChange={onCountChange}
                    // value={currentStocktakeQty}
                    precision={0}
                    // backgroundColor="yellow"
                />
            </Col>
        </Row>
    )
}

const MaxQTYModalContent = (props: any) => {
    return (
        <>
            <Row className="font-semibold text-lg pb-4">
                <Col span={8}>SKU</Col>
                <Col span={14} className="text-2xl">
                    {props.barcode}
                </Col>
            </Row>
            <Row className="font-semibold text-lg pb-4">
                <Col span={8}>Location Size</Col>
                <Col span={14} offset={1} className="text-2xl">
                    {props.locationSize}
                </Col>
            </Row>
            <Box
                keyField={props.keyField}
                quantity={props.skuMaxQty}
                onCountChange={props.onCountChange}
            ></Box>
        </>
    )
}

const CurrentQTYModalContent = (props: any) => {
    return (
        <>
            <Box
                keyField={props.keyField}
                quantity={props.skuTotalQty}
                onCountChange={props.onCountChange}
            ></Box>
            <Row className="text-red-500 font-semibold text-lg pt-4">
                <Col flex={1} offset={1}>
                    PLEASE CONFIRM YOU FOUND THIS SKU
                </Col>
                <Col flex={2}>{props.barcode}</Col>
            </Row>
        </>
    )
}

const ModalContent = ({
    keyField,
    containerAttributes,
    locationSize,
    onCountChange
}: any) => {
    const Component = {
        locationQTY: (
            <CurrentQTYModalContent
                keyField={keyField}
                skuTotalQty={containerAttributes.skuTotalQty}
                barcode={containerAttributes.barcode}
                onCountChange={onCountChange}
            />
        ),
        maxQTY: (
            <MaxQTYModalContent
                keyField={keyField}
                skuMaxQty={containerAttributes.skuMaxQty}
                locationSize={locationSize}
                barcode={containerAttributes.barcode}
            />
        )
    }
    return Component[keyField]
}

export default ModalContent
