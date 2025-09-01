import {Form, Input, InputNumber, Select} from "antd"
import React, {useImperativeHandle, useState} from "react"
import {Translation, useTranslation} from "react-i18next"

const dictionary = JSON.parse(localStorage.getItem("dictionary") || "{}")

export const AbnormalReportReason = [
    {
        label: <Translation>{(t) => t("enum.lessGoods")}</Translation>,
        value: "LESS"
    },
    {
        label: <Translation>{(t) => t("enum.damaged")}</Translation>,
        value: "BREAKAGE"
    }
]

const Abnormal = (props: any) => {
    const {value, refs} = props
    const {t} = useTranslation()
    const {data, inboundPlanOrderDetailId, onActionDispatch} = value
    const formData = JSON.parse(data)

    const [abnormalReason, setAbnormalReason] = useState<string>("")
    // const [abnormalReportReasonOptions, setAbnormalReportReasonOptions] =
    //     useState<any>([])
    const [qtyAbnormal, setQtyAbnormal] = useState<number>()

    const handleAbnormalReasonChange = (value: string) => {
        setAbnormalReason(value)
    }

    const handleQtyChange = (val: number) => {
        setQtyAbnormal(val)
    }

    // const getContainerSpecOptions = async () => {
    //     const res: any = await request({
    //         method: "post",
    //         url: `/mdm/config/dictionary/getAll`
    //     })
    //     setAbnormalReportReasonOptions(res?.data?.InboundAbnormalReason || [])
    // }

    // useEffect(() => {
    //     getContainerSpecOptions()
    // }, [])

    useImperativeHandle(refs, () => {
        return {
            abnormalReason,
            qtyAbnormal,
            inboundOrderId: formData.inboundOrderId
        }
    })

    const onSave = () => {
    }

    return (
        <div className="w-full">
            <Form
                name="complex-form"
                onFinish={onSave}
                labelCol={{span: 8}}
                wrapperCol={{span: 16}}
            >
                <Form.Item label={t("table.customerOrderNo")}>
                    <Form.Item name="customerOrderNo" noStyle>
                        <Input
                            defaultValue={formData.customerOrderNo}
                            disabled
                            style={{width: 200}}
                        />
                    </Form.Item>
                </Form.Item>
                <Form.Item label={t("skuArea.skuCode")}>
                    <Form.Item name="skuCode" noStyle>
                        <Input
                            defaultValue={formData.skuCode}
                            disabled
                            style={{width: 200}}
                        />
                    </Form.Item>
                </Form.Item>
                {Object.keys(formData.batchAttributes || {}).map(
                    (field, index) => (
                        <Form.Item label={field} required={false} key={field}>
                            <Form.Item name="field" noStyle>
                                <Input
                                    defaultValue={
                                        formData.batchAttributes[field]
                                    }
                                    disabled
                                    style={{width: "60%"}}
                                />
                            </Form.Item>
                        </Form.Item>
                    )
                )}

                <Form.Item label={t("skuArea.abnormalCause")}>
                    <Form.Item
                        name="abnormalReason"
                        noStyle
                        rules={[
                            {
                                required: true,
                                message: "abnormalReason is required"
                            }
                        ]}
                    >
                        <Select
                            defaultValue={abnormalReason}
                            style={{width: 200}}
                            options={dictionary.InboundAbnormalReason || []}
                            onChange={handleAbnormalReasonChange}
                            getPopupContainer={(e) => e.parentNode}
                        />
                    </Form.Item>
                </Form.Item>
                <Form.Item label={t("skuArea.qtyAbnormal")}>
                    <Form.Item
                        name="qtyAbnormal"
                        noStyle
                        rules={[
                            {
                                required: true,
                                message: "qtyAbnormal is required"
                            }
                        ]}
                    >
                        <InputNumber
                            value={qtyAbnormal}
                            onChange={handleQtyChange}
                            min={1}
                            style={{width: 200}}
                        />
                    </Form.Item>
                </Form.Item>

                <Form.Item label="LPN">
                    <Form.Item name="lpnCode" noStyle>
                        <Input
                            defaultValue={formData.lpnCode}
                            disabled
                            style={{width: 200}}
                        />
                    </Form.Item>
                </Form.Item>
            </Form>
        </div>
    )
}

export default Abnormal
