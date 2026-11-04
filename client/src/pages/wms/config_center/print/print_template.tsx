import schema2component from "@/utils/schema2component"
import {
    api_print_template_add,
    api_print_template_get
} from "@/pages/wms/config_center/constants/api_constant"
import { enable_options } from "@/utils/commonContants"
import { api_crud_search } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer
} from "@/utils/amis/presets"

const form = [
    {
        type: "input-text",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        type: "input-text",
        name: "templateCode",
        label: "printTemplate.templateCode",
        required: true
    },
    {
        type: "input-text",
        name: "templateName",
        label: "printTemplate.templateName",
        required: true
    },
    {
        type: "switch",
        name: "enabled",
        label: "printTemplate.enabled",
        options: enable_options
    },
    {
        type: "editor",
        name: "templateContent",
        label: "printTemplate.templateContent",
        language: "html",
        required: true
    }
]

const addDrawer = formDrawer({
    title: "printTemplate.addTitle",
    size: "xl",
    columnCount: 1,
    body: form,
    api: api_print_template_add,
    reload: "PPrintTemplate"
})

const editDrawer = formDrawer({
    title: "printTemplate.editTitle",
    size: "xl",
    columnCount: 1,
    context: "${templateCode} · ${templateName}",
    body: form,
    initApi: api_print_template_get,
    api: api_print_template_add,
    reload: "PPrintTemplate"
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn("templateCode", "printTemplate.templateCode", {
        searchable: true
    }),
    {
        name: "templateName",
        label: "printTemplate.templateName",
        searchable: true
    },
    {
        name: "enabled",
        label: "printTemplate.enabled",
        type: "mapping",
        map: enable_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "printTemplate.title",
    name: "PPrintTemplate",
    searchIdentity: "PPrintTemplate",
    api: api_crud_search,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "PPrintTemplate", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.printTemplate"
})

export default schema2component(schema)
