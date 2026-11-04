import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Descriptions, Select, Space, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { formatMillis } from "../../format"
import { REVISION_ORIGIN_LABEL_KEYS } from "../../labels"
import { RevisionOrigin, ScriptPluginRevision } from "../../types"
import DiffViewer from "../../DiffViewer"
import RollbackModal from "../RollbackModal"
import { useWorkbench } from "../context"

function scriptFileNames(left: ScriptPluginRevision, right: ScriptPluginRevision): string[] {
    const names: string[] = []
    Object.keys(left.scripts || {}).forEach((name) => {
        if (names.indexOf(name) < 0) {
            names.push(name)
        }
    })
    Object.keys(right.scripts || {}).forEach((name) => {
        if (names.indexOf(name) < 0) {
            names.push(name)
        }
    })
    return names
}

const RevisionsTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [selectedIds, setSelectedIds] = useState<string[]>([])
    const [compareFile, setCompareFile] = useState<string | null>(null)
    const [rollbackTarget, setRollbackTarget] = useState<string | null>(null)

    const selected = revisions.filter((revision) => selectedIds.indexOf(revision.id) >= 0)
    const ordered = selected.slice().sort((left, right) => left.revisionNo - right.revisionNo)
    const base = ordered.length === 2 ? ordered[0] : null
    const target = ordered.length === 2 ? ordered[1] : null
    const baseId = base ? base.id : null
    const targetId = target ? target.id : null

    useEffect(() => {
        if (!base || !target) {
            setCompareFile(null)
            return
        }
        const names = scriptFileNames(base, target)
        setCompareFile((previous) =>
            previous && names.indexOf(previous) >= 0 ? previous : names.length > 0 ? names[0] : null
        )
    }, [baseId, targetId])

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const canRollback = capabilities.canAdminister && !archived
    const fileNames = base && target ? scriptFileNames(base, target) : []
    const leftText = base && compareFile ? base.scripts[compareFile] || "" : ""
    const rightText = target && compareFile ? target.scripts[compareFile] || "" : ""
    const missingSide =
        !!base &&
        !!target &&
        !!compareFile &&
        (!Object.prototype.hasOwnProperty.call(base.scripts || {}, compareFile) ||
            !Object.prototype.hasOwnProperty.call(target.scripts || {}, compareFile))

    const columns: ColumnsType<ScriptPluginRevision> = [
        {
            title: t("pluginPlatform.revisions.revisionNo"),
            dataIndex: "revisionNo",
            width: 100,
            render: (revisionNo: number) => "v" + revisionNo
        },
        {
            title: t("pluginPlatform.revisions.origin"),
            dataIndex: "origin",
            width: 110,
            render: (origin: RevisionOrigin) => <Tag>{t(REVISION_ORIGIN_LABEL_KEYS[origin])}</Tag>
        },
        {
            title: t("pluginPlatform.revisions.changeSummary"),
            dataIndex: "changeSummary",
            ellipsis: true
        },
        {
            title: t("pluginPlatform.revisions.createdBy"),
            dataIndex: "createdBy",
            width: 140
        },
        {
            title: t("pluginPlatform.revisions.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (createdAt: string) => formatMillis(createdAt)
        },
        {
            title: t("pluginPlatform.revisions.hash"),
            dataIndex: "contentHash",
            width: 140,
            render: (hash: string) => (
                <Tooltip title={hash}>
                    <span>{hash.substring(0, 10)}</span>
                </Tooltip>
            )
        },
        {
            title: t("pluginPlatform.workbench.actions"),
            width: 110,
            render: (revision: ScriptPluginRevision) => (
                <Button
                    size="small"
                    disabled={
                        !canRollback ||
                        revision.id === plugin.currentRevisionId
                    }
                    onClick={() => setRollbackTarget(revision.id)}
                >
                    {t("pluginPlatform.revisions.rollback")}
                </Button>
            )
        }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {archived && <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />}
            <Table<ScriptPluginRevision>
                rowKey="id"
                size="small"
                loading={false}
                dataSource={revisions}
                columns={columns}
                pagination={false}
                locale={{ emptyText: t("pluginPlatform.revisions.empty") }}
                rowSelection={{
                    selectedRowKeys: selectedIds,
                    onChange: (keys) => setSelectedIds(keys.map((key) => String(key))),
                    getCheckboxProps: (revision) => ({
                        disabled: selectedIds.length >= 2 && selectedIds.indexOf(revision.id) < 0
                    })
                }}
            />
            {!base || !target ? (
                <Alert type="info" showIcon message={t("pluginPlatform.revisions.selectTwo")} />
            ) : (
                <Card size="small" title={t("pluginPlatform.revisions.compareTitle")}>
                    <Space direction="vertical" style={{ width: "100%" }}>
                        <Descriptions size="small" column={2}>
                            <Descriptions.Item label={t("pluginPlatform.revisions.base")}>
                                {"v" + base.revisionNo + " · " + formatMillis(base.createdAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label={t("pluginPlatform.revisions.target")}>
                                {"v" + target.revisionNo + " · " + formatMillis(target.createdAt)}
                            </Descriptions.Item>
                        </Descriptions>
                        <Select
                            style={{ width: 320 }}
                            value={compareFile === null ? undefined : compareFile}
                            onChange={(value: string) => setCompareFile(value)}
                            placeholder={t("pluginPlatform.revisions.file")}
                            options={fileNames.map((name) => ({ label: name, value: name }))}
                        />
                        {compareFile && missingSide && (
                            <Alert type="warning" showIcon message={t("pluginPlatform.revisions.missingFile")} />
                        )}
                        {compareFile && (
                            <DiffViewer
                                left={leftText}
                                right={rightText}
                                viewKey={base.id + ":" + target.id + ":" + compareFile}
                            />
                        )}
                    </Space>
                </Card>
            )}
            <RollbackModal
                open={rollbackTarget !== null}
                pluginKey={plugin.pluginKey}
                revisions={revisions}
                initialRevisionId={rollbackTarget}
                onCancel={() => setRollbackTarget(null)}
                onRolledBack={() => {
                    setRollbackTarget(null)
                    reload().then(() => undefined)
                }}
            />
        </Space>
    )
}

export default RevisionsTab
