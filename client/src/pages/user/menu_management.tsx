import schema2component from "@/utils/schema2component"
import {
    api_menu_add,
    api_menu_delete,
    api_menu_update
} from "@/pages/user/constants/api_constant"
import { create_update_columns, yes_no_options } from "@/utils/commonContants"
import { menu_search_api } from "@/pages/user/constants/select_search_api_constant"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "userCenter.menuManagement.form.systemCoding",
        type: "select",
        name: "systemCode",
        source: "${AppCode}",
        required: true
    },
    {
        label: "userCenter.menuManagement.form.parentMenu",
        type: "select",
        name: "parentId",
        searchable: true,
        source: menu_search_api
    },
    {
        label: "userCenter.menuManagement.form.menuType",
        type: "select",
        name: "type",
        source: "${MenuType}",
        required: true
    },
    {
        label: "userCenter.menuManagement.table.menuName",
        type: "input-text",
        name: "title",
        required: true
    },
    {
        label: "table.description",
        type: "input-text",
        name: "description"
    },
    {
        label: "userCenter.menuManagement.table.permissionID",
        type: "input-text",
        name: "permissions"
    },
    {
        label: "userCenter.menuManagement.table.sort",
        type: "input-number",
        name: "orderNum",
        required: true
    },
    {
        label: "userCenter.menuManagement.table.icon",
        type: "input-text",
        name: "icon"
    },
    {
        label: "userCenter.menuManagement.table.pathAddress",
        type: "input-text",
        name: "path"
    },
    {
        label: "userCenter.menuManagement.table.iframeDisplayed",
        type: "switch",
        name: "iframeShow",
        value: 0,
        trueValue: 1,
        falseValue: 0
    },
    {
        label: "table.status",
        type: "switch",
        name: "enable",
        value: 1,
        trueValue: 1,
        falseValue: 0,
        required: true
    }
]

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "MenuTable",
    drawer: {
        size: "lg",
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_menu_add,
            body: form,
            reload: "MenuTable"
        }
    }
}

const columns = [
    {
        hidden: true,
        name: "id",
        label: "ID"
    },
    {
        hidden: true,
        name: "parentId",
        label: "parentId"
    },
    {
        name: "title",
        label: "userCenter.menuManagement.table.menuName",
        searchable: true
    },
    {
        name: "systemCode",
        label: "userCenter.menuManagement.table.affiliation",
        type: "mapping",
        source: "${AppCode}",
        searchable: {
            type: "select",
            source: "${AppCode}"
        }
    },
    {
        name: "type",
        label: "userCenter.menuManagement.table.type",
        type: "mapping",
        source: "${MenuType}",
        searchable: {
            type: "select",
            source: "${MenuType}"
        }
    },
    {
        name: "description",
        label: "table.description"
    },
    {
        name: "permissions",
        label: "userCenter.menuManagement.table.permissionID"
    },
    {
        name: "orderNum",
        label: "userCenter.menuManagement.table.sort",
        hidden: true
    },
    {
        name: "icon",
        label: "userCenter.menuManagement.table.icon"
    },
    {
        name: "path",
        label: "userCenter.menuManagement.table.pathAddress"
    },
    {
        name: "iframeShow",
        label: "userCenter.menuManagement.table.iframeDisplayed",
        type: "mapping",
        map: yes_no_options
    },
    {
        name: "enable",
        label: "table.status",
        type: "mapping",
        map: yes_no_options
    },
    ...create_update_columns
]

const searchIdentity = "UMenu"
const showColumns = columns

const schema = {
    type: "page",
    title: "userCenter.menuManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "MenuTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns
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
                    api: {
                        method: "POST",
                        url: "/search/search?page=${1}&perPage=${100000}&createTime-op=bt"
                    },
                    fileName: "container"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 130,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    api: api_menu_update,
                                    body: form,
                                    reload: "MenuTable"
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
                            api: api_menu_delete
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
