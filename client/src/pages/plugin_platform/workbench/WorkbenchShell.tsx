import React, { useState } from "react"
import { Alert, Button, Card, Space, Spin, Tabs, Typography } from "antd"
import { ArrowLeftOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import { useHistory } from "react-router"
import PluginStatusTag from "../PluginStatusTag"
import { useWorkbench } from "./context"
import AiHistoryTab from "./tabs/AiHistoryTab"
import AuditTab from "./tabs/AuditTab"
import ConfigTab from "./tabs/ConfigTab"
import ExecutionsTab from "./tabs/ExecutionsTab"
import OverviewTab from "./tabs/OverviewTab"
import RevisionsTab from "./tabs/RevisionsTab"
import ScriptsTab from "./tabs/ScriptsTab"
import TestRunTab from "./tabs/TestRunTab"

const WorkbenchShell: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const { plugin, loading, error, reload } = useWorkbench()
    const [activeTab, setActiveTab] = useState("overview")

    const tabs = [
        {
            key: "overview",
            label: t("pluginPlatform.workbench.tab.overview"),
            children: <OverviewTab />
        },
        {
            key: "scripts",
            label: t("pluginPlatform.workbench.tab.scripts"),
            children: <ScriptsTab />
        },
        {
            key: "revisions",
            label: t("pluginPlatform.workbench.tab.revisions"),
            children: <RevisionsTab />
        },
        {
            key: "config",
            label: t("pluginPlatform.workbench.tab.config"),
            children: <ConfigTab />
        },
        {
            key: "testRun",
            label: t("pluginPlatform.workbench.tab.testRun"),
            children: <TestRunTab />
        },
        {
            key: "executions",
            label: t("pluginPlatform.workbench.tab.executions"),
            children: <ExecutionsTab />
        },
        {
            key: "aiHistory",
            label: t("pluginPlatform.workbench.tab.aiHistory"),
            children: <AiHistoryTab />
        },
        {
            key: "audit",
            label: t("pluginPlatform.workbench.tab.audit"),
            children: <AuditTab />
        }
    ]

    const refresh = () => {
        reload().then(() => undefined)
    }

    return (
        <Card>
            <Space direction="vertical" style={{ width: "100%" }} size="middle">
                <Space wrap style={{ width: "100%", justifyContent: "space-between" }}>
                    <Space wrap>
                        <Button
                            icon={<ArrowLeftOutlined />}
                            onClick={() => history.push("/plugin-platform/plugins")}
                        >
                            {t("pluginPlatform.workbench.backToList")}
                        </Button>
                        <Typography.Title level={4} style={{ margin: 0 }}>
                            {plugin ? plugin.name : t("pluginPlatform.workbench.title")}
                        </Typography.Title>
                        {plugin && <PluginStatusTag status={plugin.status} />}
                    </Space>
                    <Space wrap>
                        <Button onClick={refresh} loading={loading}>
                            {t("pluginPlatform.workbench.refresh")}
                        </Button>
                    </Space>
                </Space>
                {error && <Alert type="error" showIcon message={error} />}
                {plugin ? (
                    <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabs} />
                ) : loading ? (
                    <div style={{ textAlign: "center", padding: 32 }}>
                        <Spin />
                    </div>
                ) : (
                    <div style={{ textAlign: "center", padding: 32 }}>
                        <Button onClick={refresh}>{t("pluginPlatform.workbench.refresh")}</Button>
                    </div>
                )}
            </Space>
        </Card>
    )
}

export default WorkbenchShell
