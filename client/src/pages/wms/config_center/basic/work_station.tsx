import schema2component from "@/utils/schema2component"
import { warehouse_area_id } from "@/pages/wms/constants/select_search_api_contant"
import { true_false_options } from "@/utils/commonContants"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    requiredField,
    statusColumn
} from "@/utils/amis/presets"
import {
    api_work_station_add,
    api_work_station_config_add,
    api_work_station_config_get,
    api_work_station_get
} from "@/pages/wms/config_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

const warehouseCode = localStorage.getItem("warehouseCode")

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
        ...requiredField("validation.workstation.codeRequired")
    },
    {
        label: "table.workstationName",
        name: "stationName",
        type: "input-text",
        ...requiredField("validation.workstation.nameRequired")
    },
    {
        label: "table.warehouseAreaBelongs",
        name: "warehouseAreaId",
        type: "select",
        source: warehouse_area_id,
        ...requiredField("validation.workstation.areaRequired")
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
        source: "${dictionary.WorkStationMode}"
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
                        type: "hidden",
                        name: "workLocations[0].workLocationType",
                        value: "ROBOT"
                    },
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
                        type: "hidden",
                        name: "workLocations[1].workLocationType",
                        value: "CONVEYOR"
                    },
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
                        type: "hidden",
                        name: "workLocations[2].workLocationType",
                        value: "BUFFER_SHELVING"
                    },
                    {
                        label: "table.workBitEncoding",
                        name: "workLocations[2].workLocationCode",
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
                                label: "table.column"
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
                                        source: "${dictionary.PutWallDisplayOrder}"
                                    },
                                    {
                                        type: "select",
                                        label: "table.rightSeedingWall",
                                        name: "rightPutWallDisplayOrder",
                                        source: "${dictionary.PutWallDisplayOrder}"
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
                                        hidden: true
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
                                        source: "${dictionary.FieldColors}"
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
                                        source: "${dictionary.PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${dictionary.PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${dictionary.PtlUpdown}"
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
                                        source: "${dictionary.PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${dictionary.PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${dictionary.PtlUpdown}"
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
                                        source: "${dictionary.PtlColor}"
                                    },
                                    {
                                        type: "select",
                                        name: "mode",
                                        label: "table.tplLightMode",
                                        source: "${dictionary.PtlMode}"
                                    },
                                    {
                                        type: "select",
                                        name: "updown",
                                        label: "table.tplLightUpdown",
                                        source: "${dictionary.PtlUpdown}"
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
                                        source: "${dictionary.PtlColor}"
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
                                        source: "${dictionary.PtlColor}"
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
                                        source: "${dictionary.PtlColor}"
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

const addDrawer = formDrawer({
    title: "workstationManagement.addTitle",
    size: "xl",
    columnCount: 1,
    closeOnOutside: false,
    body: baseInfoForm,
    api: api_work_station_add,
    reload: "WorkStationTable"
})

const editDrawer = formDrawer({
    title: "workstationManagement.editTitle",
    size: "xl",
    columnCount: 1,
    closeOnOutside: false,
    context: "${stationCode} · ${stationName}",
    body: baseInfoForm,
    initApi: api_work_station_get,
    api: api_work_station_add,
    reload: "WorkStationTable"
})

const configDrawer = formDrawer({
    title: "table.workstationConfiguration",
    size: "xl",
    columnCount: 1,
    closeOnOutside: false,
    context: "${stationCode} · ${stationName}",
    body: configInfoForm,
    api: api_work_station_config_add,
    initApi: {
        method: "post",
        url: api_work_station_config_get,
        adaptor: function (payload: any) {
            // 新增配置时，删除后端返回的空 workStationId，避免将上下文中获取的 workStationId 覆盖掉
            if (payload["workStationId"] == null) {
                delete payload["workStationId"]
            }
            return payload
        }
    },
    reload: "WorkStationTable"
})

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
    codeColumn("stationCode", "table.workstationCoding", {
        searchable: true
    }),
    {
        name: "stationName",
        label: "table.workstationName"
    },
    {
        name: "workStationMode",
        label: "table.currentOperationType",
        type: "mapping",
        source: "${dictionary.WorkStationMode}"
    },
    statusColumn("workStationStatus", "table.status", {
        labels: {
            ONLINE: "status.workstationOnline",
            PAUSED: "status.workstationPaused",
            OFFLINE: "status.workstationOffline"
        },
        tones: {
            ONLINE: "success",
            PAUSED: "warning",
            OFFLINE: "muted"
        },
        searchable: {
            type: "select",
            source: "${dictionary.WorkStationStatus}"
        }
    }),
    {
        name: "workLocations",
        hidden: true
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "workstationManagement.title",
    name: "WorkStationTable",
    searchIdentity: "WWorkStation",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "WorkStationTable", drawer: addDrawer })],
    rowActions: [
        editButton(editDrawer),
        editButton(configDrawer, "table.workstationConfiguration")
    ],
    operationWidth: 180,
    empty: "emptyState.workstation"
})

export default schema2component(schema)
