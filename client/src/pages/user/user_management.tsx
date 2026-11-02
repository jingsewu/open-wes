import schema2component from "@/utils/schema2component"
import { role_search_api } from "@/pages/user/constants/select_search_api_constant"
import {
    api_user_add,
    api_user_delete,
    api_user_get,
    api_user_reset_password,
    api_user_update
} from "@/pages/user/constants/api_constant"
import { api_crud_search, api_crud_search_total } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    editButton,
    exportButton,
    formDrawer,
    linkAction,
    statusColumn
} from "@/utils/amis/presets"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "userCenter.userManagement.table.loginUsername",
        type: "input-text",
        name: "account",
        required: true
    },
    {
        label: "userCenter.userManagement.table.name",
        type: "input-text",
        name: "name",
        required: true
    },
    {
        label: "userCenter.userManagement.form.role",
        type: "select",
        name: "roleIds",
        multiple: true,
        source: role_search_api,
        required: true
    },
    {
        label: "userCenter.userManagement.table.mobilePhoneNumber",
        type: "input-text",
        name: "phone",
        required: true
    },
    {
        label: "table.email",
        type: "input-text",
        name: "email"
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
    title: "userCenter.userManagement.addTitle",
    size: "lg",
    body: form,
    api: api_user_add,
    reload: "UserTable"
})

const editDrawer = formDrawer({
    title: "userCenter.userManagement.editTitle",
    size: "lg",
    context: "${account}",
    body: form,
    initApi: api_user_get,
    api: api_user_update,
    reload: "UserTable"
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "name",
        label: "userCenter.userManagement.table.name",
        searchable: true
    },
    codeColumn("account", "userCenter.userManagement.table.loginUsername", {
        searchable: true
    }),
    {
        name: "email",
        label: "table.email",
        searchable: true
    },
    {
        name: "phone",
        label: "userCenter.userManagement.table.mobilePhoneNumber",
        searchable: true
    },
    statusColumn("status", "table.status", {
        labels: {
            "1": "common.enabled",
            "0": "common.disabled"
        },
        tones: {
            "1": "success",
            "0": "muted"
        }
    }),
    {
        name: "lastLoginIp",
        label: "userCenter.userManagement.table.LoginIP"
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "userCenter.userManagement.title",
    name: "UserTable",
    searchIdentity: "UUser",
    api: api_crud_search,
    columns,
    pageActions: [
        addButton({
            target: "UserTable",
            drawer: addDrawer
        })
    ],
    tableActions: [
        exportButton({
            api: api_crud_search_total,
            filename: "user"
        })
    ],
    rowActions: [
        editButton(editDrawer),
        deleteButton({
            api: api_user_delete,
            confirmText: "confirm.deleteUser",
            reload: "UserTable"
        }),
        linkAction({
            label: "button.resetPassword",
            api: api_user_reset_password,
            confirmText: "confirm.resetUserPassword",
            confirmTitle: "button.resetPassword"
        })
    ],
    empty: "emptyState.userManagement"
})

export default schema2component(schema)
