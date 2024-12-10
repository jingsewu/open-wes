import React from "react"
import { Table, Button } from "antd"
import type { ColumnsType } from "antd/es/table"
import { useTranslation } from "react-i18next"

interface ChoosePickingTaskTipProps {
    value: any[]
    handleConfirm: (data: any) => Promise<void>
}

const ChoosePickingTask = (props: ChoosePickingTaskTipProps) => {
    const { value = [], handleConfirm } = props
    const { t } = useTranslation()

    const columns: ColumnsType<any> = [
        {
            title: t("skuArea.skuCode"),
            dataIndex: "skuCode",
            key: "skuCode"
        },
        {
            title: t("skuArea.productName"),
            dataIndex: "skuName",
            key: "skuName"
        },
        {
            title: t("skuArea.ownerCode"),
            dataIndex: "ownerCode",
            key: "ownerCode"
        },
        {
            title: t("table.customerOrderNo"),
            dataIndex: "customerOrderNo",
            key: "customerOrderNo"
        },
        {
            title: t("table.LPNNo"),
            dataIndex: "lpnCode",
            key: "lpnCode"
        },
        {
            title: t("table.batchAttributes"),
            dataIndex: "batchAttributes",
            key: "batchAttributes",
            render: (dom, { batchAttributes = {} }) =>
                JSON.stringify(batchAttributes)
        },
        {
            title: t("table.operation"),
            key: "operation",
            fixed: "right",
            width: 100,
            render: (text, record) => (
                <Button
                    type="link"
                    className="chooseTaskConfirmBtn"
                    onClick={handleConfirm.bind(null, record)}
                >
                    {t("button.confirm")}
                </Button>
            )
        }
    ]

    return <Table columns={columns} dataSource={value} pagination={false} />
}

export default ChoosePickingTask
