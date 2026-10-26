import React, { useRef } from "react"
import { Button, Card, Empty, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { useHistory, useLocation } from "react-router"
import { WorkbenchProvider } from "./context"
import NewPluginWizard from "./wizard/NewPluginWizard"
import WorkbenchShell from "./WorkbenchShell"

const Workbench: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const location = useLocation()
    const params = new URLSearchParams(location.search)
    const mode = params.get("mode")
    const pluginKey = params.get("pluginKey")
    // Keeps the current plugin when TabsLayout restores this tab without its query string.
    const lastPluginKeyRef = useRef<string | null>(null)
    const previousModeRef = useRef<string | null>(null)
    const wizardKeyRef = useRef(0)

    if (pluginKey) {
        lastPluginKeyRef.current = pluginKey
    }
    // Query-only navigation does not remount this component; bump the key so "New plugin"
    // entered from a workbench tab starts a fresh wizard.
    if (mode === "new" && previousModeRef.current !== "new") {
        wizardKeyRef.current += 1
    }
    previousModeRef.current = mode

    if (mode === "new") {
        return <NewPluginWizard key={wizardKeyRef.current} />
    }

    const activePluginKey = pluginKey || lastPluginKeyRef.current
    if (!activePluginKey) {
        return (
            <Card>
                <Empty description={t("pluginPlatform.workbench.noPlugin")}>
                    <Typography.Paragraph type="secondary">
                        {t("pluginPlatform.workbench.noPluginHint")}
                    </Typography.Paragraph>
                    <Button type="primary" onClick={() => history.push("/plugin-platform/plugins")}>
                        {t("pluginPlatform.workbench.backToList")}
                    </Button>
                </Empty>
            </Card>
        )
    }

    return (
        <WorkbenchProvider key={activePluginKey} pluginKey={activePluginKey}>
            <WorkbenchShell />
        </WorkbenchProvider>
    )
}

export default Workbench
