import { Col, Row, AutoComplete, Select, Table } from "antd"
import type { RefObject } from "react"
import React, { useMemo, useImperativeHandle, useState, useEffect } from "react"
import { useDebounce } from "react-use"
import { useTranslation } from "react-i18next"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import request from "@/utils/requestInterceptor"

import type {
    CustomActionResponse,
    ToastFn
} from "@/pages/wms/station/instances/types"
import Counter from "@/pages/wms/station/widgets/common/Count"
import { TipType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
import {
    OperationTaskDTOS,
    ArrivedContainer,
    SkuMainDataDTO
} from "@/pages/wms/station/event-loop/types"
interface AbnormalInfo {
    /** 组件ref */
    refs: RefObject<any>
    value: {
        /** sku信息 */
        arrivedContainer: ArrivedContainer
        operationTaskDTOS: OperationTaskDTOS[]
        skuMainDataDTOS: SkuMainDataDTO[]
        shortPickingViews: any[]
        totalToBeRequiredQty: number
        /** 自定义动作接口 */
        onCustomActionDispatch: (value: any) => Promise<CustomActionResponse>
        /** 提示语接口 */
        message: ToastFn
    }
}

const AbnormalReportReasonSelector = (props: any) => {
    const { options = [], value, onChange } = props

    const handleChange = (val: string) => {
        onChange(val)
    }
    return (
        // <Select
        //     defaultValue={value}
        //     value={value}
        //     style={{ width: "100%" }}
        //     disabled={true}
        //     bordered={false}
        //     options={options}
        //     onChange={handleChange}
        //     getPopupContainer={(e) => e.parentNode}
        // />
        <Select
            showSearch
            value={value}
            style={{ width: "100%" }}
            bordered={false}
            disabled
            defaultActiveFirstOption={true}
            suffixIcon={null}
            filterOption={false}
            // onSearch={handleSearch}
            onChange={handleChange}
            notFoundContent={null}
            options={options}
            getPopupContainer={(e) => e.parentNode}
        />
    )
}

const Abnormal = (props: AbnormalInfo) => {
    const { value, refs } = props
    const { t } = useTranslation()
    const {
        operationTaskDTOS,
        skuMainDataDTOS,
        totalToBeRequiredQty,
        onCustomActionDispatch
    } = value

    const [abnormalReportReason, setAbnormalReportReason] = useState<string>("")
    const [abnormalReportReasonOptions, setAbnormalReportReasonOptions] =
        useState<any>([])

    const [pickNum, setPickNum] = useState<number>(0)
    const [isError, setIsError] = useState<boolean>(false) // count组件输入是否存在错误
    const [dataSource, setDataSource] = useState(operationTaskDTOS || [])

    useEffect(() => {
        getContainerSpecOptions()
    }, [])

    useMemo(() => {
        setDataSource(operationTaskDTOS ? [...operationTaskDTOS] : [])
    }, [operationTaskDTOS])

    const getContainerSpecOptions = async () => {
        const res: any = await request({
            method: "post",
            url: `/mdm/config/dictionary/getAll`
        })
        setAbnormalReportReasonOptions(res?.data?.StockAbnormalReason || [])
        setAbnormalReportReason(res?.data?.StockAbnormalReason?.[0].value)
    }

    const handleAbnormalReportReason = (value: string) => {
        setAbnormalReportReason(value)
    }

    const handleActualPickNumChange = (value: number) => {
        setPickNum(value)
    }

    useDebounce(
        () => {
            updateDistributionInformation()
        },
        DEBOUNCE_TIME,
        [pickNum]
    )

    const updateDistributionInformation = async () => {
        const { code, msg } = await onCustomActionDispatch({
            eventCode: TipType.REPORT_ABNORMAL_TIP,
            data: pickNum || "0"
        })
    }

    const columns = [
        {
            title: t("table.containerLatticeSlogan"),
            dataIndex: "targetLocationCode",
            key: "targetLocationCode"
        },
        {
            title: t("skuArea.numberToPick"),
            dataIndex: "requiredQty",
            key: "requiredQty"
        },
        {
            title: t("skuArea.numberPicked"),
            dataIndex: "toBeOperatedQty",
            key: "toBeOperatedQty"
        },
        {
            title: t("skuArea.quantityOfLessGoods"),
            dataIndex: "abnormalQty",
            key: "abnormalQty"
        }
    ]
    let overPickIntlMessagesID = t("skuArea.lessSKUInformation")
    // 如果是 OVER_PICK 则不显示缺货数量，提示语对应修改 ~
    if (abnormalReportReason === "OVER_PICK") {
        columns.pop()
        overPickIntlMessagesID = "播种位拣货信息"
    }
    useImperativeHandle(refs, () => {
        return {
            abnormalReportReason,
            pickNum,
            isError,
            dataSource,
            totalToBeRequiredQty
        }
    })
    console.log("abnormalReportReason", abnormalReportReason)
    return (
        <div className="w-full">
            <Row>
                <Col span={12}>
                    <div className="mr-6">
                        <div className="d-flex py-1 mb-6 text-md font-bold">
                            <div
                                className="w-1 h-6 mr-4 rounded-r"
                                style={{ background: "#004caa" }}
                            />
                            {t("skuArea.productInfo")}
                        </div>

                        <div className="d-flex flex-col my-6 w-160">
                            <div
                                className="d-flex p-2 mt-2 text-md font-bold"
                                style={{ background: "#fafafa", color: "#333" }}
                            >
                                {t("skuArea.productName")}：
                                <div className="w-80">
                                    {skuMainDataDTOS[0]?.skuName}
                                </div>
                            </div>
                            <div
                                className="d-flex p-2 mt-2 text-md font-bold"
                                style={{ background: "#fafafa", color: "#333" }}
                            >
                                {t("skuArea.barcode")}：
                                <div className="w-80">
                                    {skuMainDataDTOS[0]?.skuBarcode?.barcodes}
                                </div>
                            </div>
                            <div
                                className="d-flex p-2 mt-2 text-md font-bold items-center"
                                style={{ background: "#fafafa", color: "#333" }}
                            >
                                {t("skuArea.abnormalCause")}:
                                <div className="w-80">
                                    <AbnormalReportReasonSelector
                                        value={abnormalReportReason}
                                        onChange={handleAbnormalReportReason}
                                        options={abnormalReportReasonOptions}
                                    />
                                </div>
                            </div>

                            <div
                                className="d-flex p-2 mt-2 text-md font-bold"
                                style={{
                                    background: "#fafafa",
                                    color: "#333"
                                }}
                            >
                                {t("skuArea.numberToPick")}：
                                {totalToBeRequiredQty}
                            </div>

                            <div
                                className="d-flex p-2 mt-2 text-md font-bold items-center"
                                style={{
                                    background: "#fafafa",
                                    color: "#333"
                                }}
                            >
                                {t("skuArea.numberPicked")}：
                                <div className="w-80">
                                    <Counter
                                        onChange={handleActualPickNumChange}
                                        handleStatusChange={(status) =>
                                            setIsError(status === "error")
                                        }
                                        max={totalToBeRequiredQty - 1}
                                        value={pickNum}
                                        precision={0}
                                    />
                                    {/* <InputNumber
                                        size="large"
                                        min={0}
                                        max={totalToBeRequiredQty}
                                        precision={0}
                                        onChange={handleActualPickNumChange}
                                        style={{ width: "100%" }}
                                    /> */}
                                </div>
                            </div>
                        </div>
                    </div>
                </Col>
                <Col span={12}>
                    <div className="d-flex py-1 mb-6 text-md font-bold">
                        <div
                            className="w-1 h-6 mr-4 rounded-r"
                            style={{ background: "#004caa" }}
                        />
                        {overPickIntlMessagesID}
                    </div>
                    <Table
                        columns={columns}
                        dataSource={dataSource}
                        pagination={false}
                        size="small"
                        className="flex-1 overflow-auto"
                    />
                </Col>
            </Row>
        </div>
    )
}

export default Abnormal
