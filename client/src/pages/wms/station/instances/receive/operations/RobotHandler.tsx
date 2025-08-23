import {OperationType, WorkStationEvent} from "@/pages/wms/station/event-loop/types"
import type {replenishProps} from "../type"
import type {OperationProps} from "@/pages/wms/station/instances/types"
import React, {useEffect, useState, useRef} from "react"
import {Row, Col, Input, Divider, Button, InputNumber, Radio, message} from "antd"
import {MinusOutlined, PlusOutlined} from "@ant-design/icons"
import type {RadioChangeEvent} from "antd"
import type {InputRef} from "antd"
import request from "@/utils/requestInterceptor"
import ShelfModel from "@/pages/wms/station/widgets/common/Shelf/ShelfModel"
import {useTranslation} from "react-i18next";

let warehouseCode = localStorage.getItem("warehouseCode")

export interface ContainerHandlerConfirmProps {
    operationType: string
    operationId: string
    operationConfirmInfo: OperationConfirmInfo
}

interface OperationConfirmInfo {
    subContainerCode?: string
    containerCode?: string
}

export interface RobotHandlerProps {
    robotArea: any
    operationType: string
}

interface NumericInputProps {
    style: React.CSSProperties
    value: string
    onChange: (value: string) => void
}

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (
    data: WorkStationEvent<replenishProps> | undefined
):
    | OperationProps<RobotHandlerProps, ContainerHandlerConfirmProps>["value"]
    | Record<string, any> => {
    if (!data) return {}
    return {
        robotArea: data.workLocationArea,
        operationType: data.operationType
    }
}

const RobotHandler = (props: any) => {
    const { t } = useTranslation();

    const {value, onConfirm, focusValue, changeFocusValue, onScanSubmit,operationType} =
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
        request({
            method: "post",
            url:
                "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
                warehouseCode +
                "&containerType-op=eq&containerType=CONTAINER",
            data: {
                searchIdentity: "SearchContainerSpecCode3",
                searchObject: {
                    tables: "w_container_spec"
                },
                showColumns: [
                    {
                        dbField: "warehouse_code",
                        name: "warehouseCode",
                        javaType: "java.lang.String"
                    },
                    {
                        dbField: "container_type",
                        name: "containerType",
                        javaType: "java.lang.String"
                    },
                    {
                        dbField: "container_spec_code",
                        name: "value",
                        javaType: "java.lang.String"
                    },
                    {
                        dbField: "container_spec_name",
                        name: "label",
                        javaType: "java.lang.String"
                    },
                    {
                        dbField: "container_slot_specs",
                        name: "containerSlotSpecs",
                        javaType: "java.lang.String"
                    }
                ]
            }
        }).then((res: any) => {
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
    }, [value])

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
            }
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
                    disabled={operationType === OperationType.SELECT_CONTAINER_PUT_AWAY}
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
                                onCustomActionDispatch={(cell: any) =>
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

export default RobotHandler
