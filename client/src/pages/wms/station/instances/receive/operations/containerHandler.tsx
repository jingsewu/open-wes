import React, { useEffect, useState } from "react"
import { Button, Col, Divider, Input, InputNumber, message, Radio, Row } from "antd"
import { MinusOutlined, PlusOutlined } from "@ant-design/icons"
import type { RadioChangeEvent } from "antd"
import { useTranslation } from "react-i18next"

import ShelfModel from "@/pages/wms/station/widgets/common/Shelf/ShelfModel"
import { 
    CustomActionType,
    type ContainerHandlerProps
} from "../types"
import { 
    WAREHOUSE_CODE, 
    CONTAINER_TYPE, 
    BUTTON_STYLE 
} from "../constants"
import { 
    useContainerSpecs, 
    useFocusManagement, 
    useQuantityControl 
} from "../hooks"
import { receiveApiService, createApiHandler } from "../services/api"

// 类型定义
export interface RobotHandlerProps {
    robotArea: any
    operationType: string
}

const ContainerHandler = (props: ContainerHandlerProps) => {
    const { t } = useTranslation()

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
    } = props

    // 使用自定义Hooks
    const { containerRef, countRef } = useFocusManagement(focusValue)
    const { inputValue, setInputValue, handleQuantityChange, resetQuantity } = useQuantityControl()
    const {
        specOptions,
        containerSpec,
        containerSlotSpec,
        activeSlot,
        setActiveSlot,
        setContainerSpec,
        setContainerSlotSpec,
        initializeSpecs,
        updateContainerSpec,
        resetSpecs
    } = useContainerSpecs()

    const [containerCode, setContainerCode] = useState<string>("")

    // API错误处理
    const apiHandler = createApiHandler((error) => {
        console.error("Container operation failed:", error)
        message.error(error.message)
    })

    // 初始化容器规格数据
    useEffect(() => {
        if (!WAREHOUSE_CODE) {
            message.error(t("warehouse.code.missing"))
            return
        }

        const initialize = async () => {
            const options = await initializeSpecs(WAREHOUSE_CODE!, CONTAINER_TYPE)
            
            // 处理预设容器代码
            if (propContainerCode && propContainerCode !== containerCode) {
                setContainerCode(propContainerCode)
                setTimeout(() => onPressEnterLocal(propContainerCode, options), 0)
            }
        }

        initialize()
    }, [propContainerCode, containerCode])

    useEffect(() => {
        if (focusValue === "container") {
            setContainerCode("")
            resetQuantity()
            containerRef.current?.focus()
        } else if (focusValue === "count") {
            countRef.current?.focus()
        }
    }, [focusValue])

    const onSpecChange = (e: RadioChangeEvent) => {
        updateContainerSpec(e.target.value, specOptions)
    }

    const onSlotChange = (cell: any) => {
        setActiveSlot([cell.containerSlotSpecCode])
    }

    const onContainerChange = (e: any) => {
        setContainerCode(e.target.value)
    }

    const onPressEnterLocal = async (code: string, specOptions: any) => {
        await apiHandler(async () => {
            const containerData = await receiveApiService.getContainer(code, WAREHOUSE_CODE!)
            
            if (containerData?.containerCode) {
                const data = containerData
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
        onPressEnterLocal(containerCode, specOptions)
    }

    const handleOK = () => {
        onConfirm({ ...containerSpec, containerCode, activeSlot, inputValue })
    }

    const onContainerFull = async () => {
        await apiHandler(async () => {
            const res = await receiveApiService.completeByContainer(containerCode)

            if (res.status === 200) {
                // 重置状态
                setContainerCode("")
                resetSpecs()
                resetQuantity()
                changeFocusValue("sku")

                // 处理后续操作
                if (onScanSubmit && hasOrder) {
                    onScanSubmit()
                }

                if (isContainerLeave) {
                    containerLeave(containerCode)
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
                <div className="white-space-nowrap">
                    {t("receive.station.containerArea.scan")}:
                </div>
                <Input
                    bordered={false}
                    value={containerCode}
                    ref={containerRef}
                    onChange={onContainerChange}
                    onPressEnter={onPressEnter}
                    disabled={disable}
                />
            </div>
            <Divider style={{ margin: "12px 0" }} />
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
                                icon={<MinusOutlined />}
                                type="text"
                                onClick={handleQuantityChange.minus}
                                style={{ borderRight: BUTTON_STYLE.borderRight }}
                            />
                            <InputNumber
                                min={0}
                                ref={countRef}
                                controls={false}
                                bordered={false}
                                value={inputValue}
                                onChange={handleQuantityChange.onChange}
                            />
                            <Button
                                icon={<PlusOutlined />}
                                type="text"
                                onClick={handleQuantityChange.plus}
                                style={{ borderLeft: BUTTON_STYLE.borderLeft }}
                            />
                        </div>
                    </Col>
                </Row>
                <Row className="my-2">
                    <Col span={6}>
                        <div className="text-right leading-loose">
                            {t(
                                "receive.station.containerArea.chooseContainerSpec"
                            )}
                            ：
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
                            {t(
                                "receive.station.containerArea.chooseContainerSlot"
                            )}
                            ：
                        </div>
                    </Col>
                    <Col span={14}>
                        <Input value={activeSlot[0]} />
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
