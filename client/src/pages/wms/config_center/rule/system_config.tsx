import schema2component from "@/utils/schema2component"
import {
    api_system_config_get,
    api_system_config_save_or_update
} from "@/pages/wms/config_center/constants/api_constant"

const schema = {
    type: "page",
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "form",
            title: "systemConfigManagement.title",
            initApi: api_system_config_get,
            api: api_system_config_save_or_update,
            body: [
                {
                    type: "tabs",
                    tabs: [
                        {
                            title: "form.tab.basicInformation",
                            name: "basicConfig",
                            body: [
                                {
                                    type: "select",
                                    label: "form.system.config.basic.transferContainerReleaseMethod",
                                    source: "${dictionary.TransferContainerReleaseMethod}",
                                    name: "basicConfig.transferContainerReleaseMethod"
                                },
                                {
                                    type: "input-number",
                                    label: "form.system.config.basic.autoReleaseDelayTimeMin",
                                    name: "basicConfig.autoReleaseDelayTimeMin",
                                    hiddenOn:
                                        "${basicConfig.transferContainerReleaseMethod === 'INTERFACE'}"
                                }
                            ]
                        },
                        {
                            title: "form.tab.emsConfiguration",
                            name: "emsConfig",
                            body: [
                                {
                                    type: "switch",
                                    label: "form.system.config.ems.allowBatchCreateContainerTasks",
                                    name: "emsConfig.allowBatchCreateContainerTasks",
                                    description: ""
                                }
                            ]
                        },
                        {
                            title: "form.tab.inboundConfiguration",
                            name: "inboundConfig",
                            body: [
                                {
                                    type: "switch",
                                    label: "form.system.config.inbound.checkRepeatedCustomerOrderNo",
                                    name: "inboundConfig.checkRepeatedCustomerOrderNo",
                                    description: ""
                                },
                                {
                                    type: "switch",
                                    label: "form.system.config.inbound.checkRepeatedLpnCode",
                                    name: "inboundConfig.checkRepeatedLpnCode",
                                    description: ""
                                }
                            ]
                        },
                        {
                            title: "form.tab.outboundConfiguration",
                            name: "outboundConfig",
                            body: [
                                {
                                    type: "switch",
                                    label: "form.system.config.outbound.checkRepeatedCustomerOrderNo",
                                    name: "outboundConfig.checkRepeatedCustomerOrderNo",
                                    description: ""
                                }
                            ]
                        },
                        {
                            title: "form.tab.stockConfiguration",
                            name: "stockConfig",
                            body: [
                                {
                                    type: "switch",
                                    label: "form.system.config.stock.stockAbnormalAutoCreateAdjustmentOrder",
                                    name: "stockConfig.stockAbnormalAutoCreateAdjustmentOrder",
                                    description: ""
                                },
                                {
                                    type: "switch",
                                    label: "form.system.config.stock.adjustmentOrderAutoCompleteAdjustment",
                                    name: "stockConfig.adjustmentOrderAutoCompleteAdjustment",
                                    description: ""
                                },
                                {
                                    type: "input-number",
                                    label: "form.system.config.stock.zeroStockSavedDays",
                                    name: "stockConfig.zeroStockSavedDays",
                                    description: ""
                                }
                            ]
                        },
                        {
                            title: "form.tab.outboundAlgoConfiguration",

                            name: "outboundAlgoConfig",
                            body: [
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "modal.cutoffTime",
                                            name: "outboundAlgoConfig.cutoffTime",
                                            description:
                                                "modal.cutoffTime.description"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.algoMode",
                                            name: "outboundAlgoConfig.mode"
                                        }
                                    ]
                                },
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "modal.shareRackPoolMaxStationDistance",
                                            name: "outboundAlgoConfig.shareRackPoolMaxStationDistance",
                                            precision: 2
                                        },
                                        {
                                            type: "input-number",
                                            label: "modal.maxHitNum",
                                            name: "outboundAlgoConfig.maxHitNum"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.orderDispatchStrategy",
                                            source: "${dictionary.OrderDispatchStrategy}",
                                            name: "outboundAlgoConfig.orderDispatchStrategy"
                                        }
                                    ]
                                },
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "modal.orderDispatchBalanceOffset",
                                            name: "outboundAlgoConfig.orderDispatchBalanceOffset",
                                            description:
                                                "modal.orderDispatchBalanceOffset.description"
                                        },
                                        {
                                            type: "input-text",
                                            label: "modal.firstHitRackSide",
                                            source: "${dictionary.FirstHitRackSide}",
                                            name: "outboundAlgoConfig.firstHitRackSide"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.algoName",
                                            source: "${dictionary.OrderDispatchHitAlgoName}",
                                            name: "outboundAlgoConfig.algoName"
                                        }
                                    ]
                                },
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "modal.maxOnTheWayRackNum",
                                            name: "outboundAlgoConfig.maxOnTheWayRackNum",
                                            description:
                                                "modal.maxOnTheWayRackNum.description"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.taskBalanceDimension",
                                            source: "${dictionary.TaskBalanceDimension}",
                                            name: "outboundAlgoConfig.taskBalanceDimension"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.warehouseLogicTypePriority",
                                            description:
                                                "modal.warehouseLogicTypePriority.description",
                                            source: "${dictionary.WarehouseLogicType}",
                                            multiple: true,
                                            joinValues: false,
                                            extractValue: true,
                                            name: "outboundAlgoConfig.warehouseLogicTypePriority"
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
}

export default schema2component(schema)
