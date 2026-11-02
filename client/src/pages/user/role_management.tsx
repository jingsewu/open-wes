import schema2component from "@/utils/schema2component"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    editButton,
    exportButton,
    formDrawer,
    statusColumn
} from "@/utils/amis/presets"
import {
    api_role_add,
    api_role_delete,
    api_role_get_role_menu,
    api_role_update,
    api_role_update_role_menu
} from "@/pages/user/constants/api_constant"
import { warehouse_code_search_api } from "@/pages/user/constants/select_search_api_constant"
import { api_crud_search, api_crud_search_total } from "@/pages/constantApi"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "userCenter.roleManagement.table.roleCoding",
        type: "input-text",
        name: "code",
        required: true
    },
    {
        label: "userCenter.roleManagement.table.roleName",
        type: "input-text",
        name: "name",
        required: true
    },
    {
        label: "userCenter.roleManagement.form.repositoryPermissions",
        type: "select",
        multiple: true,
        search: true,
        name: "warehouseCodes",
        source: warehouse_code_search_api
    },
    {
        label: "table.description",
        type: "input-text",
        name: "description"
    },
    {
        label: "table.status",
        type: "switch",
        name: "status",
        value: 1,
        trueValue: 1,
        falseValue: 0
    }
]

const addDrawer = formDrawer({
    title: "userCenter.roleManagement.addTitle",
    body: form,
    api: api_role_add,
    reload: "RoleTable"
})

const editDrawer = formDrawer({
    title: "userCenter.roleManagement.editTitle",
    body: form,
    initApi: "get:/user/api/role/${id}",
    api: api_role_update,
    reload: "RoleTable"
})

const permissionsDrawer = formDrawer({
    title: "userCenter.roleManagement.assignPermissionsTitle",
    size: "lg",
    body: [
        {
            type: "tpl",
            tpl: "${name}",
            className: "owes-drawer-context"
        },
        {
            type: "input-tree",
            name: "menus",
            label: "userCenter.roleManagement.form.menuPermissions",
            labelField: "title",
            valueField: "id",
            multiple: true,
            cascade: true,
            initiallyOpen: false,
            autoCheckChildren: false,
            searchable: true,
            height: 480,
            source: api_role_get_role_menu
        }
    ],
    api: api_role_update_role_menu
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "name",
        label: "userCenter.roleManagement.table.roleName",
        searchable: true
    },
    codeColumn("code", "userCenter.roleManagement.table.roleCoding", {
        searchable: true
    }),
    statusColumn("status", "table.status", {
        labels: {
            "1": "common.enabled",
            "0": "common.disabled"
        },
        tones: {
            "1": "success",
            "0": "muted"
        },
        searchable: true
    }),
    ...auditColumns()
]

const schema = crudPage({
    title: "userCenter.roleManagement.title",
    name: "RoleTable",
    searchIdentity: "URole",
    api: api_crud_search,
    columns,
    empty: "emptyState.roleManagement",
    pageActions: [
        addButton({
            target: "RoleTable",
            drawer: addDrawer
        })
    ],
    tableActions: [
        exportButton({
            api: api_crud_search_total,
            filename: "roles"
        })
    ],
    rowActions: [
        editButton(editDrawer),
        deleteButton({
            api: api_role_delete,
            reload: "RoleTable"
        }),
        {
            type: "button",
            actionType: "drawer",
            level: "link",
            className: "owes-action",
            label: "button.assignPermissions",
            drawer: permissionsDrawer
        }
    ]
})

export default schema2component(schema)
