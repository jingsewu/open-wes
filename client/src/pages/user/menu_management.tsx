import schema2component from "@/utils/schema2component"
import {
    api_menu_add,
    api_menu_delete,
    api_menu_update
} from "@/pages/user/constants/api_constant"
import { yes_no_options } from "@/utils/commonContants"
import { menu_search_api } from "@/pages/user/constants/select_search_api_constant"
import { api_crud_search, api_crud_search_total } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    crudPage,
    deleteButton,
    editButton,
    exportButton,
    formDrawer,
    statusColumn
} from "@/utils/amis/presets"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "userCenter.menuManagement.form.systemCoding",
        type: "select",
        name: "systemCode",
        source: "${dictionary.AppCode}",
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
        source: "${dictionary.MenuType}",
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

const addDrawer = formDrawer({
    title: "userCenter.menuManagement.addTitle",
    size: "lg",
    body: form,
    api: api_menu_add,
    reload: "MenuTable"
})

const editDrawer = formDrawer({
    title: "userCenter.menuManagement.editTitle",
    size: "lg",
    context: "${title}",
    body: form,
    api: api_menu_update,
    reload: "MenuTable"
})

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
        source: "${dictionary.AppCode}",
        searchable: {
            type: "select",
            source: "${dictionary.AppCode}"
        }
    },
    {
        name: "type",
        label: "userCenter.menuManagement.table.type",
        type: "mapping",
        source: "${dictionary.MenuType}",
        searchable: {
            type: "select",
            source: "${dictionary.MenuType}"
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
    statusColumn("enable", "table.status", {
        labels: {
            "1": "common.enabled",
            "0": "common.disabled"
        },
        tones: {
            "1": "success",
            "0": "muted"
        }
    }),
    ...auditColumns()
]

const schema = crudPage({
    title: "userCenter.menuManagement.title",
    name: "MenuTable",
    searchIdentity: "UMenu",
    api: api_crud_search,
    columns,
    pageActions: [
        addButton({
            target: "MenuTable",
            drawer: addDrawer
        })
    ],
    tableActions: [
        exportButton({
            api: api_crud_search_total,
            filename: "menu"
        })
    ],
    rowActions: [
        editButton(editDrawer),
        deleteButton({
            api: api_menu_delete,
            confirmText: "confirm.deleteMenu",
            reload: "MenuTable"
        })
    ],
    empty: "emptyState.menuManagement"
})

export default schema2component(schema)
