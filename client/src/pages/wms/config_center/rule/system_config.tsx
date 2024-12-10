import schema2component from "@/utils/schema2component"
import {api_system_config_get, api_system_config_save_or_update} from "@/pages/wms/config_center/constants/api_constant"

const schema = {
    type: "page",
    // title: "systemConfigManagement.title",
    initApi: "post:/mdm/config/dictionary/getAll",
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
                            title: "form.tab.emsConfiguration",
                            name: "emsConfig",
                            body: [
                                {
                                    type: "switch",
                                    label: "是否批量创建容器任务",
                                    name: "emsConfig.allowBatchCreateContainerTasks",
                                    description: "----"
                                }
                            ]
                        },
                        {
                            title: "form.tab.inboundConfiguration",
                            name: "inboundConfig",

                        },
                        {
                            title: "form.tab.outboundConfiguration",
                            name: "outboundConfig",
                            body: [
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "form.config.pickingOrderFetchSizeWhenEveryTimeDispatching",
                                            name: "outboundConfig.pickingOrderFetchSizeWhenEveryTimeDispatching",
                                            description: "form.config.pickingOrderFetchSizeWhenEveryTimeDispatching.description"
                                        },
                                        {
                                            type: "input-number",
                                            label: "form.config.undoOperationTaskFetchSizeWhenEveryTimeDispatching",
                                            name: "outboundConfig.undoOperationTaskFetchSizeWhenEveryTimeDispatching",
                                            description: "form.config.undoOperationTaskFetchSizeWhenEveryTimeDispatching.description"
                                        }
                                    ]
                                },
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "input-number",
                                            label: "form.config.operationTaskFetchSizeWhenContainerArrived",
                                            name: "outboundConfig.operationTaskFetchSizeWhenContainerArrived",
                                            description: "form.config.operationTaskFetchSizeWhenContainerArrived.description"
                                        },
                                        {
                                            type: "switch",
                                            label: "form.config.checkArrivedContainerForDispatchedOrder",
                                            name: "outboundConfig.checkArrivedContainerForDispatchedOrder",
                                            description: "form.config.checkArrivedContainerForDispatchedOrder.description"
                                        },
                                        {
                                            type: "select",
                                            label: "form.config.excludeCustomerOrderTypes",
                                            multiple: true,
                                            joinValues: false,
                                            extractValue: true,
                                            name: "outboundConfig.excludeCustomerOrderTypes",
                                            source: "${CustomerOrderType}"
                                        }
                                    ]
                                },
                                {
                                    type: "group",
                                    body: [
                                        {
                                            type: "switch",
                                            label: "form.config.autoUnlockTransferContainer",
                                            name: "outboundConfig.autoUnlockTransferContainer"
                                        },
                                        {
                                            type: "input-number",
                                            label: "form.config.autoUnlockTransferContainerTimeInMillis",
                                            name: "outboundConfig.autoUnlockTransferContainerTimeInMillis"
                                        },
                                        {
                                            type: "input-number",
                                            label: "form.config.autoUnlockTransferContainerSizeEveryTime",
                                            name: "outboundConfig.autoUnlockTransferContainerSizeEveryTime"
                                        }
                                    ]
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
                                            description: "modal.cutoffTime.description"
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
                                            source: "${OrderDispatchStrategy}",
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
                                            description: "modal.orderDispatchBalanceOffset.description",
                                        },
                                        {
                                            type: "input-text",
                                            label: "modal.firstHitRackSide",
                                            source: "${FirstHitRackSide}",
                                            name: "outboundAlgoConfig.firstHitRackSide"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.algoName",
                                            source: "${OrderDispatchHitAlgoName}",
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
                                            description: "modal.maxOnTheWayRackNum.description",
                                        },
                                        {
                                            type: "select",
                                            label: "modal.taskBalanceDimension",
                                            source: "${TaskBalanceDimension}",
                                            name: "outboundAlgoConfig.taskBalanceDimension"
                                        },
                                        {
                                            type: "select",
                                            label: "modal.warehouseLogicTypePriority",
                                            description: "modal.warehouseLogicTypePriority.description",
                                            source: "${WarehouseLogicType}",
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
