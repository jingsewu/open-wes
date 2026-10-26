import React from "react"
import { Tag } from "antd"
import { useTranslation } from "react-i18next"
import { PluginStatus } from "./types"
import { STATUS_COLORS, STATUS_LABEL_KEYS } from "./labels"

interface PluginStatusTagProps {
    status: PluginStatus
}

const PluginStatusTag: React.FC<PluginStatusTagProps> = ({ status }) => {
    const { t } = useTranslation()
    return <Tag color={STATUS_COLORS[status]}>{t(STATUS_LABEL_KEYS[status])}</Tag>
}

export default PluginStatusTag
