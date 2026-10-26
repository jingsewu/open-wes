import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Modal, Spin, Steps } from "antd"
import { useTranslation } from "react-i18next"
import { useHistory } from "react-router"
import { getContract, getPlugin } from "../../api"
import { AiDraftResult, PluginContract, ScriptPlugin } from "../../types"
import usePluginCapabilities from "../../usePluginCapabilities"
import BasicsStep from "./BasicsStep"
import RequirementStep from "./RequirementStep"
import ReviewStep from "./ReviewStep"
import FinishStep from "./FinishStep"

const NewPluginWizard: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const capabilities = usePluginCapabilities()
    const [current, setCurrent] = useState(0)
    const [plugin, setPlugin] = useState<ScriptPlugin | null>(null)
    const [aiResult, setAiResult] = useState<AiDraftResult | null>(null)
    const [contract, setContract] = useState<PluginContract | null>(null)

    const applySystem = plugin ? plugin.applySystem : null

    useEffect(() => {
        if (!applySystem) {
            return
        }
        let active = true
        getContract(applySystem)
            .then((loaded) => {
                if (active) {
                    setContract(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setContract(null)
                }
            })
        return () => {
            active = false
        }
    }, [applySystem])

    const refreshPlugin = () => {
        if (!plugin) {
            return Promise.resolve()
        }
        return getPlugin(plugin.pluginKey)
            .then((detail) => setPlugin(detail))
            .catch(() => undefined)
    }

    const cancel = () => {
        Modal.confirm({
            title: t("pluginPlatform.wizard.cancel"),
            content: t("pluginPlatform.wizard.cancelConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => history.push("/plugin-platform/plugins")
        })
    }

    if (capabilities.loading) {
        return (
            <Card>
                <Spin />
            </Card>
        )
    }
    if (!capabilities.canDevelop) {
        return (
            <Card>
                <Alert type="warning" showIcon message={t("pluginPlatform.wizard.noPermission")} />
            </Card>
        )
    }

    const steps = [
        { title: t("pluginPlatform.wizard.step.basics") },
        { title: t("pluginPlatform.wizard.step.requirement") },
        { title: t("pluginPlatform.wizard.step.review") },
        { title: t("pluginPlatform.wizard.step.finish") }
    ]

    return (
        <Card
            title={t("pluginPlatform.wizard.title")}
            extra={<Button onClick={cancel}>{t("pluginPlatform.wizard.cancel")}</Button>}
        >
            <Steps current={current} items={steps} style={{ marginBottom: 24 }} />
            {current === 0 && (
                <BasicsStep
                    onCreated={(created) => {
                        setPlugin(created)
                        setCurrent(1)
                    }}
                />
            )}
            {current === 1 && plugin && (
                <RequirementStep
                    plugin={plugin}
                    contract={contract}
                    aiResult={aiResult}
                    onResult={(result) => {
                        setAiResult(result)
                        refreshPlugin()
                    }}
                    onNext={() => setCurrent(2)}
                />
            )}
            {current === 2 && plugin && (
                <ReviewStep
                    plugin={plugin}
                    aiResult={aiResult}
                    contract={contract}
                    onSaved={refreshPlugin}
                    onNext={() => setCurrent(3)}
                />
            )}
            {current === 3 && plugin && (
                <FinishStep
                    plugin={plugin}
                    canAdminister={capabilities.canAdminister}
                    onRefresh={refreshPlugin}
                    onClose={() =>
                        history.push(
                            "/plugin-platform/plugin-workbench?pluginKey=" +
                                encodeURIComponent(plugin.pluginKey)
                        )
                    }
                />
            )}
        </Card>
    )
}

export default NewPluginWizard
