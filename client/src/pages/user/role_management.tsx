import schema2component from "@/utils/schema2component"
import { create_update_columns, yes_no_options } from "@/utils/commonContants"
import {
    api_role_add,
    api_role_delete,
    api_role_get_role_menu,
    api_role_update,
    api_role_update_role_menu
} from "@/pages/user/constants/api_constant"
import { warehouse_code_search_api } from "@/pages/user/constants/select_search_api_constant"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "userCenter.roleManagement.table.roleCoding",
        type: "input-text",
        name: "code"
    },
    {
        label: "userCenter.roleManagement.table.roleName",
        type: "input-text",
        name: "name"
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

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "RoleTable",
    drawer: {
        size: "lg",
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_role_add,
            body: form,
            reload: "RoleTable"
        }
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: "true"
    },
    {
        name: "name",
        label: "userCenter.roleManagement.table.roleName",
        searchable: true
    },
    {
        name: "code",
        label: "userCenter.roleManagement.table.roleCoding",
        searchable: true
    },
    {
        name: "status",
        label: "table.status",
        type: "mapping",
        map: yes_no_options
    },
    ...create_update_columns
]

const menuTree = [
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
        source: api_role_get_role_menu
    }
]

const searchIdentity = "URole"
const showColumns = columns

const schema = {
    type: "page",
    title: "userCenter.roleManagement.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "RoleTable",
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
                                    initApi: "get:/user/api/role/${id}",
                                    api: api_role_update,
                                    body: form,
                                    reload: "RoleTable"
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
                            api: api_role_delete
                        },
                        {
                            label: "button.assignPermissions",
                            type: "button",
                            actionType: "drawer",
                            drawer: {
                                title: "button.assignPermissions",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    api: api_role_update_role_menu,
                                    body: menuTree
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
