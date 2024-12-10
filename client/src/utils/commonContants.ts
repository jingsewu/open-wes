export const yes_no_options = [
    {
        value: 1,
        label: "是",
        "en-US": {
            label: "Yes"
        }
    },
    {
        value: 0,
        label: "否",
        "en-US": {
            label: "No"
        }
    }
]

export const true_false_options = [
    {
        value: true,
        label: "是",
        "en-US": {
            label: "Yes"
        }
    },
    {
        value: false,
        label: "否",
        "en-US": {
            label: "No"
        }
    }
]

export const enable_options = [
    {
        value: true,
        label: "启用",
        "en-US": {
            label: "Enable"
        }
    },
    {
        value: false,
        label: "禁用",
        "en-US": {
            label: "Disabled"
        }
    }
]

export const create_update_columns = [
    {
        label: "table.createdBy",
        name: "createUser"
    },
    {
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        label: "table.modifiedBy",
        name: "updateUser"
    },
    {
        label: "table.updated",
        name: "updateTime",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]
