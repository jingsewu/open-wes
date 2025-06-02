import schema2component from "@/utils/schema2component"
import {
    api_plugin_config,
    api_plugin_install,
    api_plugin_tenant_config,
    api_plugin_uninstall
} from "@/pages/plugin_platform/constants/api_constant"

const configForm = {
    type: "form",
    api: api_plugin_config,
    body: [
        {
            type: "hidden",
            name: "id"
        },
        {
            type: "hidden",
            name: "version"
        },
        {
            label: "插件配置",
            type: "textarea",
            name: "configInfo",
            required: true
        }
    ]
}

const schema = {
    type: "page",
    title: "pluginSystem.pluginStore.title,",
    toolbar: [],
    initApi: "/plugin/pluginManage/storeQuery",
    body: {
        type: "cards",
        source: "$pluginStore",
        card: {
            body: [
                {
                    hidden: true,
                    label: "插件id",
                    name: "id"
                },
                {
                    hidden: true,
                    label: "pluginSystem.pluginManagement.table.pluginCoding",
                    name: "code"
                },
                {
                    label: "pluginSystem.pluginManagement.table.pluginName",
                    name: "name"
                },
                {
                    label: "pluginSystem.pluginManagement.table.developers",
                    name: "developer"
                },
                {
                    label: "pluginSystem.pluginManagement.table.pluginVersion",
                    name: "pluginVersion"
                }
            ],
            actions: [
                {
                    label: "pluginSystem.pluginStore.table.installation",
                    type: "button",
                    level: "link",
                    icon: "arrow-down",
                    actionType: "ajax",
                    confirmText: "确认安装？",
                    api: api_plugin_install
                },
                {
                    label: "pluginSystem.pluginStore.table.unload",
                    type: "button",
                    level: "link",
                    icon: "arrow-up",
                    actionType: "ajax",
                    confirmText: "确认卸载？",
                    api: api_plugin_uninstall
                },
                {
                    label: "pluginSystem.pluginStore.table.parameterSettings",
                    type: "button",
                    level: "link",
                    icon: "arrow-up",
                    actionType: "drawer",
                    drawer: {
                        title: "button.modify,",
                        body: {
                            type: "form",
                            initApi: api_plugin_tenant_config,
                            api: api_plugin_config,
                            body: [
                                {
                                    type: "hidden",
                                    name: "id"
                                },
                                {
                                    type: "hidden",
                                    name: "version"
                                },
                                {
                                    label: "pluginSystem.pluginStore.table.pluginConfiguration",
                                    type: "textarea",
                                    name: "configInfo",
                                    required: true
                                }
                            ]
                        }
                    }
                }
            ]
        }
    }
}

export default schema2component(schema)
