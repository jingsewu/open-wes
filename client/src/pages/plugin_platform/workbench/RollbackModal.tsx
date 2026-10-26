import React, { useEffect, useState } from "react"
import { Modal, Select, message } from "antd"
import { useTranslation } from "react-i18next"
import { rollbackPlugin } from "../api"
import { formatMillis } from "../format"
import { REVISION_ORIGIN_LABEL_KEYS } from "../labels"
import { ScriptPluginRevision } from "../types"

interface RollbackModalProps {
    open: boolean
    pluginKey: string
    revisions: ScriptPluginRevision[]
    initialRevisionId: string | null
    onCancel: () => void
    onRolledBack: () => void
}

const RollbackModal: React.FC<RollbackModalProps> = ({
    open,
    pluginKey,
    revisions,
    initialRevisionId,
    onCancel,
    onRolledBack
}) => {
    const { t } = useTranslation()
    const [selectedId, setSelectedId] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    useEffect(() => {
        if (open) {
            setSelectedId(initialRevisionId || (revisions.length > 0 ? revisions[0].id : null))
        }
    }, [open, initialRevisionId, revisions.length])

    const submit = () => {
        if (!selectedId) {
            return
        }
        setSubmitting(true)
        rollbackPlugin(pluginKey, selectedId)
            .then(() => {
                message.success(t("pluginPlatform.overview.rolledBack"))
                onRolledBack()
            })
            .catch(() => undefined)
            .then(() => setSubmitting(false))
    }

    return (
        <Modal
            title={t("pluginPlatform.revisions.rollbackTitle")}
            visible={open}
            onCancel={onCancel}
            onOk={submit}
            confirmLoading={submitting}
            okText={t("pluginPlatform.common.confirm")}
            cancelText={t("pluginPlatform.common.cancel")}
            okButtonProps={{ disabled: !selectedId }}
        >
            <Select
                style={{ width: "100%" }}
                value={selectedId === null ? undefined : selectedId}
                onChange={(value: string) => setSelectedId(value)}
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
    )
}

export default RollbackModal
