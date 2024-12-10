import React from "react"
import schema2component from "@/utils/schema2component"
import {volume} from "@/pages/wms/config_center/constants/form_constants"
import {Translation} from "react-i18next"
import {create_update_columns} from "@/utils/commonContants"
import {
    api_container_spec_add,
    api_container_spec_delete,
    api_container_spec_get,
    api_container_spec_update
} from "@/pages/wms/config_center/constants/api_constant"

let warehouseCode = localStorage.getItem("warehouseCode")

const fromBody = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "table.specificationCode",
        type: "input-text",
        name: "containerSpecCode",
        required: true
    },
    {
        label: "table.specificationName",
        type: "input-text",
        name: "containerSpecName",
        required: true
    },
    {
        label: "table.containerType",
        type: "select",
        name: "containerType",
        source: "${ContainerType}",
        required: true
    },
    {
        type: "hidden",
        name: "warehouseCode",
        label: "table.warehouseCode",
        value: warehouseCode
    },
    ...volume,
    {
        type: "input-table",
        name: "containerSlotSpecs",
        addable: true,
        editable: true,
        removable: true,
        needConfirm: false,
        columns: [
            ...volume,
            {
                name: "containerSlotSpecCode",
                label: "table.containerLatticeSlogan",
                type: "input-text",
                required: true
            },
            {
                name: "face",
                label: "table.face/wall_coding",
                type: "input-text",
                value: "",
                hiddenOn: "this.containerType == 'CONTAINER' || this.containerType == 'TRANSFER_CONTAINER' ",
                requiredOn: "this.containerType == 'SHELF'",
            },
            {
                name: "level",
                label: "workLocationArea.layer",
                type: "input-text",
                required: true
            },
            {
                name: "bay",
                label: "table.column",
                type: "input-number",
                required: true
            },
            {
                name: "locLevel",
                label: "格口所在的层",
                type: "input-number",
                required: true
            },
            {
                name: "locBay",
                label: "格口所在的列",
                type: "input-number",
                required: true
            }
        ]
    }
]

const form = {
    type: "form",
    api: api_container_spec_add,
    body: fromBody
}

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    dialog: {
        size: "full",
        title: "button.add",
        closeOnEsc: true,
        body: form
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
        name: "containerSpecCode",
        label: "table.specificationCode",
        searchable: true
    },
    {
        name: "containerSpecName",
        label: "table.specificationName",
        searchable: true
    },
    {
        name: "containerType",
        label: "table.containerType",
        type: "mapping",
        source: "${ContainerType}",
        searchable: {
            type: "select",
            source: "${ContainerType}"
        }
    },
    {
        label: "table.volume(mm³)",
        name: "volume",
        tpl: "${volume} mm³"
    },
    {
        label: "table.length(mm)",
        name: "length",
        tpl: "${length} mm"
    },
    {
        label: "table.width(mm)",
        name: "width",
        tpl: "${width} mm"
    },
    {
        label: "table.height(mm)",
        name: "height",
        tpl: "${height} mm"
    },
    ...create_update_columns
]

const searchIdentity = "WContainerSpec"
const schema = {
    type: "page",
    //   title: "容器规格管理",
    title: <Translation>{(t) => t("containerSpec.title")}</Translation>,
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "ContainerSpecTable",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: columns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [
                "reload",
                add,
                {
                    type: "export-excel",
                    label: "button.export",
                    api:
                        "post:/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                        warehouseCode,
                    fileName: "container_spec"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 100,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                size: "xl",
                                body: {
                                    type: "form",
                                    initApi: api_container_spec_get,
                                    api: api_container_spec_update,
                                    controls: fromBody
                                }
                            }
                        },
                        {
                            label: "button.delete",
                            type: "button",
                            actionType: "ajax",
                            level: "danger",
                            confirmText: "toast.sureDelete",
                            confirmTitle: "button.delete",
                            api: api_container_spec_delete,
                            reload: "ContainerSpecTable"
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
