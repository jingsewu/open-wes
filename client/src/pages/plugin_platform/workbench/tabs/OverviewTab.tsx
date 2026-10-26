import React, { useState } from "react"
import {
    Alert,
    Button,
    Card,
    Descriptions,
    Form,
    Input,
    Modal,
    Select,
    Space,
    Tag,
    Typography,
    message
} from "antd"
import { useTranslation } from "react-i18next"
import {
    archivePlugin,
    disablePlugin,
    enablePlugin,
    rollbackPlugin,
    updatePluginMetadata,
    validatePlugin
} from "../../api"
import { REVISION_ORIGIN_LABEL_KEYS, VALIDATION_LABEL_KEYS } from "../../labels"
import { formatMillis } from "../../format"
import { ValidationReport } from "../../types"
import PluginStatusTag from "../../PluginStatusTag"
import ValidationReportView from "../../ValidationReportView"
import { useWorkbench } from "../context"

const OverviewTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [metadataForm] = Form.useForm()
    const [savingMetadata, setSavingMetadata] = useState(false)
    const [busyAction, setBusyAction] = useState<string | null>(null)
    const [report, setReport] = useState<ValidationReport | null>(null)
    const [disableOpen, setDisableOpen] = useState(false)
    const [disableReason, setDisableReason] = useState("")
    const [rollbackOpen, setRollbackOpen] = useState(false)
    const [rollbackRevisionId, setRollbackRevisionId] = useState<string | null>(null)

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const canEditMetadata = capabilities.canDevelop && !archived
    const canValidate = capabilities.canDevelop && !archived
    const canEnable = capabilities.canAdminister && !archived && plugin.status !== "ENABLED"
    const canDisable = capabilities.canAdminister && plugin.status === "ENABLED"
    const canArchive = capabilities.canAdminister && !archived
    const canRollback = capabilities.canAdminister && !archived && revisions.length > 0
    const currentRevision =
        revisions.filter((revision) => revision.id === plugin.currentRevisionId)[0] || null
    const applySystemLabel =
        plugin.applySystem === "wes"
            ? t("pluginPlatform.applySystem.wes")
            : plugin.applySystem === "station"
            ? t("pluginPlatform.applySystem.station")
            : plugin.applySystem
    const lastValidationLabel = plugin.lastValidationStatus
        ? t(VALIDATION_LABEL_KEYS[plugin.lastValidationStatus] || plugin.lastValidationStatus)
        : t("pluginPlatform.validation.notValidated")

    const saveMetadata = (values: { name: string; description?: string; applyModule?: string }) => {
        setSavingMetadata(true)
        updatePluginMetadata(plugin.pluginKey, {
            name: values.name,
            description: values.description || "",
            applyModule: values.applyModule || ""
        })
            .then(() => {
                message.success(t("pluginPlatform.overview.metadataSaved"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setSavingMetadata(false))
    }

    const handleValidate = () => {
        setBusyAction("validate")
        validatePlugin(plugin.pluginKey)
            .then((validationReport) => {
                setReport(validationReport)
                message.success(t("pluginPlatform.overview.validationFinished"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    const handleEnable = () => {
        Modal.confirm({
            title: t("pluginPlatform.overview.enable"),
            content: t("pluginPlatform.overview.enableConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => {
                setBusyAction("enable")
                return enablePlugin(plugin.pluginKey)
                    .then(() => {
                        message.success(t("pluginPlatform.overview.enabled"))
                        return reload()
                    })
                    .catch(() => undefined)
                    .then(() => setBusyAction(null))
            }
        })
    }

    const submitDisable = () => {
        setBusyAction("disable")
        disablePlugin(plugin.pluginKey, disableReason)
            .then(() => {
                setDisableOpen(false)
                setDisableReason("")
                message.success(t("pluginPlatform.overview.disabled"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    const handleArchive = () => {
        Modal.confirm({
            title: t("pluginPlatform.overview.archive"),
            content: t("pluginPlatform.overview.archiveConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => {
                setBusyAction("archive")
                return archivePlugin(plugin.pluginKey)
                    .then(() => {
                        message.success(t("pluginPlatform.overview.archived"))
                        return reload()
                    })
                    .catch(() => undefined)
                    .then(() => setBusyAction(null))
            }
        })
    }

    const openRollback = () => {
        setRollbackRevisionId(revisions.length > 0 ? revisions[0].id : null)
        setRollbackOpen(true)
    }

    const submitRollback = () => {
        if (!rollbackRevisionId) {
            return
        }
        setBusyAction("rollback")
        rollbackPlugin(plugin.pluginKey, rollbackRevisionId)
            .then(() => {
                setRollbackOpen(false)
                message.success(t("pluginPlatform.overview.rolledBack"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Card title={t("pluginPlatform.overview.title")} size="small">
                <Descriptions size="small" bordered column={2}>
                    <Descriptions.Item label={t("pluginPlatform.workbench.pluginKey")}>
                        {plugin.pluginKey}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.list.column.status")}>
                        <PluginStatusTag status={plugin.status} />
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.applySystem")}>
                        {applySystemLabel}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.applyModule")}>
                        {plugin.applyModule || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.sdkVersion")}>
                        {plugin.sdkVersion || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.currentRevision")}>
                        {currentRevision ? (
                            <Space size={4}>
                                <Tag color="blue">{"v" + currentRevision.revisionNo}</Tag>
                                <span>{t(REVISION_ORIGIN_LABEL_KEYS[currentRevision.origin])}</span>
                            </Space>
                        ) : (
                            t("pluginPlatform.overview.noRevisions")
                        )}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.validation.lastValidation")}>
                        {lastValidationLabel}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.validation.lastAt")}>
                        {formatMillis(plugin.lastValidationAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.updatedAt")}>
                        {formatMillis(plugin.updateTime)}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.overview.description")}>
                        {plugin.description || "-"}
                    </Descriptions.Item>
                </Descriptions>
                {plugin.lastValidationStatus === "FAILED" && plugin.lastValidationError && (
                    <Alert
                        style={{ marginTop: 12 }}
                        type="error"
                        showIcon
                        message={t("pluginPlatform.validation.lastError")}
                        description={
                            <Typography.Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                                {plugin.lastValidationError}
                            </Typography.Paragraph>
                        }
                    />
                )}
            </Card>

            <Card title={t("pluginPlatform.overview.statusActions")} size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Space wrap>
                        {capabilities.canDevelop && (
                            <Button onClick={handleValidate} loading={busyAction === "validate"} disabled={!canValidate}>
                                {t("pluginPlatform.overview.validate")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button
                                type="primary"
                                onClick={handleEnable}
                                loading={busyAction === "enable"}
                                disabled={!canEnable}
                            >
                                {t("pluginPlatform.overview.enable")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button onClick={() => setDisableOpen(true)} disabled={!canDisable}>
                                {t("pluginPlatform.overview.disable")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button danger onClick={handleArchive} loading={busyAction === "archive"} disabled={!canArchive}>
                                {t("pluginPlatform.overview.archive")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button onClick={openRollback} disabled={!canRollback}>
                                {t("pluginPlatform.overview.rollback")}
                            </Button>
                        )}
                    </Space>
                    {!capabilities.canAdminister && (
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.overview.noPermission")}
                        </Typography.Text>
                    )}
                    {archived && (
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.scripts.readOnlyArchived")}
                        </Typography.Text>
                    )}
                </Space>
            </Card>

            {report && (
                <Card title={t("pluginPlatform.validation.reportTitle")} size="small">
                    <ValidationReportView report={report} />
                </Card>
            )}

            <Card title={t("pluginPlatform.overview.metadata")} size="small">
                <Form
                    form={metadataForm}
                    layout="vertical"
                    initialValues={{
                        name: plugin.name,
                        description: plugin.description || "",
                        applyModule: plugin.applyModule || ""
                    }}
                    onFinish={saveMetadata}
                >
                    <Form.Item
                        name="name"
                        label={t("pluginPlatform.overview.name")}
                        rules={[
                            {
                                required: true,
                                message: t("pluginPlatform.wizard.basics.nameRequired")
                            }
                        ]}
                    >
                        <Input maxLength={128} disabled={!canEditMetadata} />
                    </Form.Item>
                    <Form.Item name="description" label={t("pluginPlatform.overview.description")}>
                        <Input.TextArea rows={3} maxLength={512} disabled={!canEditMetadata} />
                    </Form.Item>
                    <Form.Item name="applyModule" label={t("pluginPlatform.workbench.applyModule")}>
                        <Input maxLength={64} disabled={!canEditMetadata} />
                    </Form.Item>
                    {canEditMetadata && (
                        <Form.Item style={{ marginBottom: 0 }}>
                            <Button type="primary" htmlType="submit" loading={savingMetadata}>
                                {t("pluginPlatform.overview.saveMetadata")}
                            </Button>
                        </Form.Item>
                    )}
                </Form>
            </Card>

            <Modal
                title={t("pluginPlatform.overview.disableTitle")}
                visible={disableOpen}
                onCancel={() => setDisableOpen(false)}
                onOk={submitDisable}
                confirmLoading={busyAction === "disable"}
                okText={t("pluginPlatform.common.confirm")}
                cancelText={t("pluginPlatform.common.cancel")}
            >
                <Input
                    value={disableReason}
                    onChange={(event) => setDisableReason(event.target.value)}
                    placeholder={t("pluginPlatform.overview.disableReason")}
                    maxLength={255}
                />
            </Modal>

            <Modal
                title={t("pluginPlatform.overview.rollbackTitle")}
                visible={rollbackOpen}
                onCancel={() => setRollbackOpen(false)}
                onOk={submitRollback}
                confirmLoading={busyAction === "rollback"}
                okText={t("pluginPlatform.common.confirm")}
                cancelText={t("pluginPlatform.common.cancel")}
                okButtonProps={{ disabled: !rollbackRevisionId }}
            >
                <Select
                    style={{ width: "100%" }}
                    value={rollbackRevisionId === null ? undefined : rollbackRevisionId}
                    onChange={(value: string) => setRollbackRevisionId(value)}
                    placeholder={t("pluginPlatform.overview.rollbackRevision")}
                    options={revisions.map((revision) => ({
                        label:
                            "v" +
                            revision.revisionNo +
                            " · " +
                            formatMillis(revision.createdAt) +
                            " · " +
                            t(REVISION_ORIGIN_LABEL_KEYS[revision.origin]),
                        value: revision.id
                    }))}
                />
            </Modal>
        </Space>
    )
}

export default OverviewTab
