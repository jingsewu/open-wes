import type { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type { replenishProps } from "../type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import React, { useEffect, useState } from "react"
import { Row, Col, Input, Divider, Button, InputNumber, Radio } from "antd"
import { MinusOutlined, PlusOutlined } from "@ant-design/icons"
import type { RadioChangeEvent } from "antd"
import request from "@/utils/requestInterceptor"
import ShelfModel from "@/pages/wms/station/widgets/common/Shelf/ShelfModel"

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

export enum MachineType {
    TANK_FEEDING_ROBOT = "TANK_FEEDING_ROBOT", // ("料箱机器人")
    LITTLE_LATENT_ROBOT = "LITTLE_LATENT_ROBOT", // ("小潜伏机器人-搬料箱")
    LARGE_LATENT_ROBOT = "LARGE_LATENT_ROBOT" // ("大潜伏机器人-搬料架")
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
        // operationId: data.operationId,
        operationType: data.operationType
    }
}

const RobotHandler = (props: any) => {
    const { value, onConfirm } = props

    const [inputValue, setInputValue] = useState<number>()
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
            const data = res.data
            setContainerSpec({
                containerSpecCode: data.containerSpecCode,
                containerId: data.id
            })
        })
    }

    const handleOK = () => {
        console.log("activeSlotrobot", activeSlot)
        onConfirm({ ...containerSpec, containerCode, activeSlot, inputValue })
    }

    const onContainerFull = () => {
        request({
            method: "post",
            url: `/wms/inbound/accept/completeByContainer?containerCode=${containerCode}`
        }).then((res: any) => {
            console.log("onContainerFull", res)
        })
    }

    return (
        <div className="bg-white p-4 h-full">
            <div className="d-flex items-center">
                <div className="white-space-nowrap">请扫描容器号:</div>
                <Input
                    bordered={false}
                    value={containerCode}
                    onChange={onContainerChange}
                    onPressEnter={onPressEnter}
                />
            </div>
            <Divider style={{ margin: "12px 0" }} />
            <div className="px-10">
                <Row>
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            输入收货数量：
                        </div>
                    </Col>
                    <Col>
                        <div className="border border-solid	">
                            <Button
                                icon={<MinusOutlined />}
                                type="text"
                                onClick={handleMinus}
                                // size={size}
                                style={{ borderRight: "1px solid #ccc" }}
                            />
                            <InputNumber
                                min={0}
                                // max={10}
                                controls={false}
                                bordered={false}
                                value={inputValue}
                                onChange={onChange}
                            />
                            <Button
                                icon={<PlusOutlined />}
                                type="text"
                                onClick={handlePlus}
                                // size={size}
                                style={{ borderLeft: "1px solid #ccc" }}
                            />
                        </div>
                    </Col>
                </Row>
                <Row className="my-2">
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            容器规格选择：
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
                            style={{ height: 160 }}
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
                            请输入格口号：
                        </div>
                    </Col>
                    <Col span={14}>
                        <Input value={activeSlot[0]} />
                    </Col>
                </Row>
                <Row justify="end" className="mt-2">
                    <Col span={8}>
                        <Button type="primary" onClick={onContainerFull}>
                            满箱
                        </Button>
                        <Button className="ml-2" onClick={handleOK}>
                            确定
                        </Button>
                    </Col>
                </Row>
            </div>
        </div>
    )
}

export default RobotHandler
