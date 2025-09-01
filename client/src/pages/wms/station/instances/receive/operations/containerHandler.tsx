import {WorkStationMode} from "@/pages/wms/station/event-loop/types"
import React, {useEffect, useRef, useState} from "react"
import type {InputRef, RadioChangeEvent} from "antd"
import {Button, Col, Divider, Input, InputNumber, message, Radio, Row} from "antd"
import {MinusOutlined, PlusOutlined} from "@ant-design/icons"
import request from "@/utils/requestInterceptor"
import ShelfModel from "@/pages/wms/station/widgets/common/Shelf/ShelfModel"
import {useTranslation} from "react-i18next";
import {request_select_container_spec} from "@/pages/wms/constants/select_search_api_contant";

let warehouseCode = localStorage.getItem("warehouseCode")

export interface RobotHandlerProps {
    robotArea: any
    operationType: string
}

const ContainerHandler = (props: any) => {
    const {t} = useTranslation();

    const {workStationEvent, onConfirm, focusValue, changeFocusValue, onScanSubmit} =
        props
    const containerRef = useRef<InputRef>(null)
    const countRef = useRef<any>(null)
    const [inputValue, setInputValue] = useState<number | string>()
    const [specOptions, setSpecOptions] = useState<any[]>([])
    const [containerSpec, setContainerSpec] = useState<any>({})
    const [containerSlotSpec, setContainerSlotSpec] = useState<string>("")
    const [activeSlot, setActiveSlot] = useState<string[]>([])
    const [containerCode, setContainerCode] = useState<string>("")

    useEffect(() => {
        if (!warehouseCode) {
            message.error(t("warehouse.code.missing"));
            return;
        }

        request_select_container_spec(warehouseCode, "CONTAINER")
            .then((res: any) => {
                console.log("res", res?.data?.options)
                setSpecOptions(res?.data?.options || [])
                setContainerSpec({
                    containerSpecCode: res?.data?.options[0]?.value
                })
                const slotSpec = res?.data?.options[0]?.containerSlotSpecs
                setContainerSlotSpec(JSON.parse(slotSpec || "[]"))
            })
    }, [])

    useEffect(() => {
        setInputValue("")
        const containerCode = workStationEvent?.workLocationArea?.workLocationViews?.length > 0
        && workStationEvent.workLocationArea.workLocationViews[0].workLocationSlots?.length > 0
            ? workStationEvent.workLocationArea.workLocationViews[0].workLocationSlots[0].arrivedContainer?.containerCode
            : undefined;

        if (containerCode) {
            setContainerCode(containerCode);
        }
    }, [workStationEvent])

    useEffect(() => {
        if (focusValue === "container") {
            setContainerCode("")
            setInputValue("")
            containerRef.current?.focus()
        } else if (focusValue === "count") {
            countRef.current?.focus()
        }
    }, [focusValue])

    const onChange = (value: number) => {
        setInputValue(value)
    }

    const handleMinus = () => {
        if (!inputValue) return
        setInputValue((prev: number) => prev - 1)
    }

    const handlePlus = () => {
        setInputValue((prev: number) => prev + 1)
    }

    const onSpecChange = (e: RadioChangeEvent) => {
        console.log(`radio checked:${e.target.value}`)
        setContainerSpec({
            ...containerSpec,
            containerSpecCode: e.target.value
        })
        const slotSpec = specOptions.find(
            (item) => item.value === e.target.value
        )?.containerSlotSpecs
        setContainerSlotSpec(JSON.parse(slotSpec))
    }

    const onSlotChange = (cell: any) => {
        console.log("cell", cell)
        setActiveSlot([cell.containerSlotSpecCode])
    }

    const onContainerChange = (e: any) => {
        setContainerCode(e.target.value)
    }

    const onPressEnter = () => {
        request({
            method: "post",
            url: `/wms/basic/container/get?containerCode=${containerCode}&warehouseCode=${warehouseCode}`
        }).then((res: any) => {
            console.log("containerCode", res)
            if (res.data?.containerCode) {
                const data = res.data
                setContainerSpec({
                    containerSpecCode: data.containerSpecCode,
                    containerId: data.id
                })
                const slotSpec = specOptions.find(
                    (item) => item.value === data.containerSpecCode
                )?.containerSlotSpecs
                setContainerSlotSpec(JSON.parse(slotSpec))
                changeFocusValue("count")
            } else {
                message.error("Container is not exits")
            }
        })
    }

    const handleOK = () => {
        console.log("activeSlot", activeSlot)
        onConfirm({...containerSpec, containerCode, activeSlot, inputValue})
    }

    const onContainerFull = () => {
        request({
            method: "post",
            url: `/wms/inbound/accept/completeByContainer?containerCode=${containerCode}`
        }).then((res: any) => {
            console.log("onContainerFull", res)
            if (res.status === 200) {
                setContainerCode("")
                setContainerSpec({})
                setContainerSlotSpec("")
                setActiveSlot([])
                setInputValue("")
                changeFocusValue("sku")
                onScanSubmit()

                if (workStationEvent?.workStationMode === WorkStationMode.SELECT_CONTAINER_PUT_AWAY) {
                    containerLeave(containerCode);
                }
            }
        })
    }

    const containerLeave = (containerCode: string) => {
        request({
            method: "post",
            url: "station/api?apiCode=CONTAINER_LEAVE",
            data: containerCode
        }).then((res: any) => {
            console.log("containerLeave", res)
        })
    }

    return (
        <div className="bg-white p-4 h-full">
            <div className="d-flex items-center">
                <div className="white-space-nowrap">{t("receive.station.containerArea.scan")}:</div>
                <Input
                    bordered={false}
                    value={containerCode}
                    ref={containerRef}
                    onChange={onContainerChange}
                    onPressEnter={onPressEnter}
                    disabled={workStationEvent.workStationMode === WorkStationMode.SELECT_CONTAINER_PUT_AWAY}
                />
            </div>
            <Divider style={{margin: "12px 0"}}/>
            <div className="px-10">
                <Row>
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            {t("receive.station.containerArea.receiveQty")}：
                        </div>
                    </Col>
                    <Col>
                        <div className="border border-solid	">
                            <Button
                                icon={<MinusOutlined/>}
                                type="text"
                                onClick={handleMinus}
                                // size={size}
                                style={{borderRight: "1px solid #ccc"}}
                            />
                            <InputNumber
                                min={0}
                                // max={10}
                                ref={countRef}
                                controls={false}
                                bordered={false}
                                value={inputValue}
                                onChange={onChange}
                            />
                            <Button
                                icon={<PlusOutlined/>}
                                type="text"
                                onClick={handlePlus}
                                // size={size}
                                style={{borderLeft: "1px solid #ccc"}}
                            />
                        </div>
                    </Col>
                </Row>
                <Row className="my-2">
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            {t("receive.station.containerArea.chooseContainerSpec")}：
                        </div>
                    </Col>
                    <Col span={14}>
                        <div>
                            <Radio.Group
                                value={containerSpec.containerSpecCode}
                                buttonStyle="solid"
                                onChange={onSpecChange}
                            >
                                {specOptions.map((item) => (
                                    <Radio.Button value={item.value}>
                                        {item.label}
                                    </Radio.Button>
                                ))}
                            </Radio.Group>
                        </div>
                        <div
                            className="d-flex flex-col"
                            style={{height: 160}}
                        >
                            <ShelfModel
                                containerSlotSpecs={containerSlotSpec}
                                activeSlotCodes={activeSlot}
                                showAllSlots={true}
                                showLevel={false}
                                onActionDispatch={(cell: any) =>
                                    onSlotChange(cell)
                                }
                            />
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            {t("receive.station.containerArea.chooseContainerSlot")}：
                        </div>
                    </Col>
                    <Col span={14}>
                        <Input value={activeSlot[0]}/>
                    </Col>
                </Row>
                <Row justify="end" className="mt-2">
                    <Col span={8}>
                        <Button type="primary" onClick={onContainerFull}>
                            {t("receive.station.button.full")}
                        </Button>
                        <Button className="ml-2" onClick={handleOK}>
                            {t("receive.station.button.confirm")}
                        </Button>
                    </Col>
                </Row>
            </div>
        </div>
    )
}

export default ContainerHandler
