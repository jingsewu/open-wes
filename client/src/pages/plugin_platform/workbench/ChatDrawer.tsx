import React, { useState } from "react"
import { Alert, Drawer, Select, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { formatMillis } from "../format"
import AiDraftPanel from "./AiDraftPanel"
import { useWorkbench } from "./context"

interface ChatDrawerProps {
    open: boolean
    onClose: () => void
}

const ChatDrawer: React.FC<ChatDrawerProps> = ({ open, onClose }) => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [baseRevisionId, setBaseRevisionId] = useState<string | null>(null)
    const [lastRevisionNo, setLastRevisionNo] = useState<number | null>(null)

    if (!plugin) {
        return null
    }

    const revisionOptions = [
        { label: t("pluginPlatform.chat.baseRevisionNone"), value: "" }
    ].concat(
        revisions.map((revision) => ({
            label: "v" + revision.revisionNo + " · " + formatMillis(revision.createdAt),
            value: revision.id
        }))
    )

    return (
        <Drawer
            title={t("pluginPlatform.chat.title")}
            placement="right"
            width={560}
            visible={open}
            onClose={onClose}
        >
            {!capabilities.canDevelop ? (
                <Alert type="warning" showIcon message={t("pluginPlatform.chat.noPermission")} />
            ) : (
                <Space direction="vertical" style={{ width: "100%" }} size="middle">
                    <div>
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.chat.baseRevision")}
                        </Typography.Text>
                        <Select
                            style={{ width: "100%", marginTop: 4 }}
                            value={baseRevisionId === null ? "" : baseRevisionId}
                            onChange={(value: string) => setBaseRevisionId(value === "" ? null : value)}
                            options={revisionOptions}
                        />
                    </div>
                    {lastRevisionNo !== null && (
                        <Alert
                            type="success"
                            showIcon
                            message={t("pluginPlatform.chat.completed", { no: lastRevisionNo })}
                        />
                    )}
                    <AiDraftPanel
                        pluginKey={plugin.pluginKey}
                        baseRevisionId={baseRevisionId}
                        onCompleted={(result) => {
                            setLastRevisionNo(result.revisionNo)
                            reload().then(() => undefined)
                        }}
                    />
                </Space>
            )}
        </Drawer>
    )
}

export default ChatDrawer
