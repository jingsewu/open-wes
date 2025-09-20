import React, {useEffect, useRef, useState} from "react"
import type {InputRef, RadioChangeEvent} from "antd"
import {Button, Col, Divider, Input, InputNumber, message, Radio, Row} from "antd"
import {MinusOutlined, PlusOutlined} from "@ant-design/icons"
import request from "@/utils/requestInterceptor"
import ShelfModel from "@/pages/wms/station/widgets/common/Shelf/ShelfModel"
import {useTranslation} from "react-i18next";
import {request_select_container_spec} from "@/pages/wms/constants/select_search_api_contant";
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType";

let warehouseCode = localStorage.getItem("warehouseCode")

export interface RobotHandlerProps {
    robotArea: any
    operationType: string
}

interface ContainerHandlerProps {
    focusValue: string
    onConfirm: any
    changeFocusValue: any
    onScanSubmit?: any
    containerCode?: string
    disable?: boolean
    isContainerLeave?: boolean
    hasOrder?: boolean
    onActionDispatch: any
}

const ContainerHandler = (props: ContainerHandlerProps) => {
    const {t} = useTranslation();

    const {
        disable,
        containerCode: propContainerCode,
        isContainerLeave,
        onConfirm,
        focusValue,
        changeFocusValue,
        onScanSubmit,
        hasOrder,
        onActionDispatch
    } =
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

                if (propContainerCode && propContainerCode !== containerCode) {
                    setContainerCode(propContainerCode);
                    // Delay the execution to ensure state is updated
                    setTimeout(() => {
                        onPressEnterLocal(propContainerCode, res?.data?.options || []);
                    }, 0);
                }
            })

    }, [propContainerCode, containerCode]);

    useEffect(() => {
        setInputValue("")
    }, [])

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

    const onPressEnterLocal = (code: string, specOptions: any) => {
        request({
            method: "post",
            url: `/wms/basic/container/get?containerCode=${code}&warehouseCode=${warehouseCode}`
        }).then((res: any) => {
            console.log("containerCode", res)
            if (res.data?.containerCode) {
                const data = res.data
                setContainerSpec({
                    containerSpecCode: data.containerSpecCode,
                    containerId: data.id
                })
                const slotSpec = specOptions.find(
                    (item: any) => item.value === data.containerSpecCode
                )?.containerSlotSpecs
                setContainerSlotSpec(JSON.parse(slotSpec))
                changeFocusValue("count")
            } else {
                message.error("Container is not exits")
            }
        })
    }

    const onPressEnter = () => {
        onPressEnterLocal(containerCode, specOptions);
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
            if (res.status === 200) {
                setContainerCode("")
                setContainerSpec({})
                setContainerSlotSpec("")
                setActiveSlot([])
                setInputValue("")
                changeFocusValue("sku")

                if (onScanSubmit && hasOrder) {
                    onScanSubmit()
                }

                if (isContainerLeave) {
                    containerLeave(containerCode);
                }
            }
        })
    }

    const containerLeave = (containerCode: string) => {
        onActionDispatch({
            eventCode: CustomActionType.CONTAINER_LEAVE,
            data: containerCode
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
                    disabled={disable}
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
