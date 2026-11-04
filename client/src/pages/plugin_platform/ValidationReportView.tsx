import React from "react"
import { Alert, Descriptions, Table } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ValidationFailure, ValidationReport } from "./types"
import { formatMillis } from "./format"

interface ValidationReportViewProps {
    report: ValidationReport
}

const ValidationReportView: React.FC<ValidationReportViewProps> = ({ report }) => {
    const { t } = useTranslation()
    const failures = report.failures || []
    const columns: ColumnsType<ValidationFailure> = [
        {
            title: t("pluginPlatform.validation.stage"),
            dataIndex: "stage",
            width: 180
        },
        {
            title: t("pluginPlatform.validation.message"),
            dataIndex: "message"
        }
    ]

    return (
        <div>
            <Alert
                type={report.passed ? "success" : "error"}
                showIcon
                message={
                    report.passed
                        ? t("pluginPlatform.validation.passed")
                        : t("pluginPlatform.validation.failed")
                }
                style={{ marginBottom: 12 }}
            />
            <Descriptions size="small" bordered column={2}>
                <Descriptions.Item label={t("pluginPlatform.workbench.pluginKey")}>
                    {report.pluginKey}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.workbench.applySystem")}>
                    {report.targetApp}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.revisions.revisionNo")}>
                    {report.revisionId}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.validation.lastAt")}>
                    {formatMillis(report.finishedAt)}
                </Descriptions.Item>
            </Descriptions>
            {failures.length > 0 && (
                <Table<ValidationFailure>
                    rowKey={(failure) => failure.stage + ":" + failure.message}
                    size="small"
                    style={{ marginTop: 12 }}
                    pagination={false}
                    dataSource={failures}
                    columns={columns}
                />
            )}
        </div>
    )
}

export default ValidationReportView
