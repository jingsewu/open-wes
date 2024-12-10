import schema2component from "@/utils/schema2component"
import { role_search_api } from "@/pages/user/constants/select_search_api_constant"
import {
    api_user_add,
    api_user_delete,
    api_user_get,
    api_user_reset_password,
    api_user_update
} from "@/pages/user/constants/api_constant"
import { create_update_columns, yes_no_options } from "@/utils/commonContants"

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

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "UserTable",
    drawer: {
        size: "lg",
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_user_add,
            body: form,
            reload: "UserTable"
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
        name: "name",
        label: "userCenter.userManagement.table.name",
        searchable: true
    },
    {
        name: "account",
        label: "userCenter.userManagement.table.loginUsername",
        searchable: true
    },
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
    {
        name: "status",
        label: "table.status",
        type: "mapping",
        map: yes_no_options
    },
    {
        name: "lastLoginIp",
        label: "userCenter.userManagement.table.LoginIP"
    },
    ...create_update_columns
]

const searchIdentity = "UUser"
const showColumns = columns

const schema = {
    type: "page",
    title: "userCenter.userManagement.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "UserTable",
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
                    fileName: "user"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 200,
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
                                    initApi: api_user_get,
                                    api: api_user_update,
                                    body: form,
                                    reload: "UserTable"
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
                            api: api_user_delete
                        },
                        {
                            label: "button.resetPassword",
                            type: "button",
                            actionType: "ajax",
                            confirmText: "toast.sureResetPassword",
                            confirmTitle: "button.reset",
                            api: api_user_reset_password
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
