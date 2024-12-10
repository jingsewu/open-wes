import schema2component from "@/utils/schema2component"
import {warehouse_area_id} from "@/pages/wms/constants/select_search_api_contant"
import {create_update_columns, true_false_options} from "@/utils/commonContants"
import {
    api_work_station_add,
    api_work_station_config_add,
    api_work_station_config_get,
    api_work_station_get
} from "@/pages/wms/config_center/constants/api_constant";

let warehouseCode = localStorage.getItem("warehouseCode")

const baseInfoForm = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "table.workstationCoding",
        name: "stationCode",
        type: "input-text",
        required: true
    },
    {
        label: "table.workstationName",
        name: "stationName",
        type: "input-text",
        required: true
    },
    {
        label: "table.warehouseAreaBelongs",
        name: "warehouseAreaId",
        type: "select",
        source: warehouse_area_id,
        required: true
    },
    {
        label: "table.taskRules",
        name: "stationRuleId",
        type: "select"
    },
    {
        label: "table.operationalBusiness",
        name: "allowWorkStationModes",
        type: "select",
        multiple: true,
        source: "${WorkStationMode}"
    },
    {
        label: "table.position",
        name: "position",
        type: "combo",
        items: [
            {
                name: "x",
                label: "x",
                type: "input-number"
            },
            {
                name: "y",
                label: "y",
                type: "input-number"
            },
            {
                name: "z",
                label: "z",
                type: "input-number"
            }
        ]
    },
    {
        type: "tabs",
        tabs: [
            {
                title: "table.robotWorkstations",
                body: [
                    {
                        label: "table.workBitEncoding",
                        name: "workLocations[0].workLocationCode",
                        type: "input-text",
                        requiredOn: "${workLocations[0].enable}"
                    },
                    {
                        label: "table.whetherEnabled",
                        name: "workLocations[0].enable",
                        type: "switch"
                    }
                ]
            },
            {
                title: "table.conveyorLines",
                body: [
                    {
                        label: "table.workBitEncoding",
                        name: "workLocations[1].workLocationCode",
                        type: "input-text",
                        requiredOn: "${workLocations[1].enable}"
                    },
                    {
                        label: "table.whetherEnabled",
                        name: "workLocations[1].enable",
                        type: "switch"
                    }
                ]
            },
            {
                title: "table.cacheShelves",
                body: [
                    {
                        label: "table.workBitEncoding",
                        name: "workLocations[2].workLocationCode}",
                        type: "input-text",
                        requiredOn: "${workLocations[2].enable}"
                    },
                    {
                        label: "table.whetherEnabled",
                        name: "workLocations[2].enable",
                        type: "switch"
                    },
                    {
                        name: "workLocations[2].workLocationSlots",
                        label: "table.workingCompartmentInformation",
                        type: "input-table",
                        addable: true,
                        editable: true,
                        columns: [
                            {
                                name: "workLocationCode",
                                type: "input-text",
                                label: "table.workBitEncoding"
                            },
                            {
                                name: "groupCode",
                                type: "input-text",
                                label: "table.groupNumber"
                            },
                            {
                                name: "slotCode",
                                type: "input-text",
                                label: "table.gridSlogan"
                            },
                            {
                                name: "level",
                                type: "input-number",
                                label: "workLocationArea.layer"
                            },
                            {
                                name: "bay",
                                type: "input-number",
                                label: "table.colum"
                            },
                            {
                                name: "enable",
                                type: "switch",
                                label: "table.whetherEnabled"
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        type: "hidden",
        name: "warehouseCode",
        label: "仓库编码",
        value: warehouseCode
    }
]

const configInfoForm = [
    {
        name: "configId",
        type: "input-text",
        hidden: true
    },
    {
        name: "version",
        type: "input-text",
        hidden: true
    },
    {
        name: "workStationId",
        type: "input-text",
        value: "${id}",
        hidden: true
    },
    {
        type: "tabs",
        tabs: [
            {
                title: "table.outboundConfiguration",
                body: [
                    {
                        type: "combo",
                        name: "pickingStationConfig",
                        items: [
                            {
                                type: "switch",
                                label: "table.emptyContainerRecycling",
                                name: "emptyToteRecycle",
                                options: true_false_options
                            },
                            {
                                type: "switch",
                                label: "table.mergeBatchPicking",
                                name: "mergeBatchPicking",
                                options: true_false_options
                            },
                            {
                                type: "switch",
                                label: "table.validateCustomerWaveNoBeforeBound",
                                name: "validateCustomerWaveNoBeforeBound",
                                options: true_false_options
                            },
                            {
                                type: "switch",
                                label: "table.validateTransferContainerPicking",
                                name: "validateTransferContainerPicking",
                                options: true_false_options
                            }
                        ]
                    },
                    {
                        type: "divider"
                    },
                    {
                        type: "fieldSet",
                        title: "table.seedingWallDisplayOrder",
                        body: [
                            {
                                type: "combo",
                                name: "pickingStationConfig",
                                items: [
                                    {
                                        type: "select",
                                        label: "table.leftSeedingWall",
                                        name: "leftPutWallDisplayOrder",
                                        source: "${PutWallDisplayOrder}"
                                    },
                                    {
                                        type: "select",
                                        label: "table.rightSeedingWall",
                                        name: "rightPutWallDisplayOrder",
                                        source: "${PutWallDisplayOrder}"
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        type: "divider"
                    },
                    {
                        type: "fieldSet",
                        title: "table.seedingWallGridField",
                        body: [
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallSlotFields",
                                multiple: true,
                                addable: false,
                                removable: false,
                                draggable: true,
                                items: [
                                    {
                                        type: "input-text",
                                        name: "fieldName",
                                        label: "table.fieldName",
                                        hidden: "true"
                                    },
                                    {
                                        type: "input-text",
                                        name: "fieldDesc",
                                        label: "table.fieldDescription",
                                        readOnly: "true"
                                    },
                                    {
                                        type: "switch",
                                        name: "display",
                                        label: "table.whetherDisplayed"
                                    },
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.fieldColor",
                                        source: "${FieldColors}"
                                    },
                                    {
                                        type: "checkbox",
                                        name: "bold",
                                        label: "table.whetherBold"
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        type: "divider"
                    },
                    {
                        type: "fieldSet",
                        title: "table.seedingWallTplConfig",
                        body: [
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.waitingBinding",
                                label: "putWallArea.bindBox",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${PtlUpdown}"
                                    }
                                ]
                            },
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.dispatch",
                                label: "putWallArea.sowing",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${PtlUpdown}"
                                    }
                                ]
                            },
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.waitingSeal",
                                label: "putWallArea.seal",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${PtlUpdown}"
                                    }
                                ]
                            },
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.disabled",
                                label: "putWallArea.disabled",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
                                    }
                                ]
                            },
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.selected",
                                label: "putWallArea.selected",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
                                    }
                                ]
                            },
                            {
                                type: "combo",
                                name: "pickingStationConfig.putWallTagConfig.optional",
                                label: "putWallArea.optional",
                                items: [
                                    {
                                        type: "select",
                                        name: "color",
                                        label: "table.tplLightColor",
                                        source: "${PtlColor}"
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

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    dialog: {
        title: "button.add",
        size: "lg",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_work_station_add,
            body: baseInfoForm
        }
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaBelongs",
        type: "mapping",
        source: warehouse_area_id,
        searchable: {
            type: "select",
            source: warehouse_area_id
        }
    },
    {
        name: "stationCode",
        label: "table.workstationCoding",
        searchable: true
    },
    {
        name: "stationName",
        label: "table.workstationName"
    },
    {
        name: "workStationMode",
        label: "table.currentOperationType",
        type: "mapping",
        source: "${WorkStationMode}"
    },
    {
        name: "workStationStatus",
        label: "table.status",
        type: "mapping",
        source: "${WorkStationStatus}",
        searchable: {
            type: "select",
            source: "${WorkStationStatus}"
        }
    },
    {
        name: "workLocations",
        hidden: true
    },
    ...create_update_columns
]

const searchIdentity = "WWorkStation"
const showColumns = columns

const schema = {
    type: "page",
    title: "workstationManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "role",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [add],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 180,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "button.modify",
                                size: "lg",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    api: api_work_station_add,
                                    initApi: api_work_station_get,
                                    body: baseInfoForm
                                }
                            }
                        },
                        {
                            label: "table.workstationConfiguration",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "table.workstationConfiguration",
                                size: "lg",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    api: api_work_station_config_add,
                                    initApi: {
                                        method: "get",
                                        url: api_work_station_config_get,
                                        adaptor: function (payload: any) {
                                            // 新增配置时，删除后端返回的空 workStationId，避免将上下文中获取的 workStationId 覆盖掉
                                            if (
                                                payload["workStationId"] == null
                                            ) {
                                                delete payload["workStationId"]
                                            }
                                            return payload
                                        }
                                    },
                                    body: configInfoForm
                                }
                            }
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
