import schema2component from "@/utils/schema2component"
import {
    api_owner_add,
    api_owner_get,
    api_owner_update
} from "@/pages/wms/config_center/constants/api_constant"
import { create_update_columns } from "@/utils/commonContants"
import {
    city,
    country,
    district,
    province
} from "@/pages/wms/constants/select_search_api_contant"

let warehouseCode = localStorage.getItem("warehouseCode")

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "skuArea.ownerCode",
        type: "input-text",
        name: "ownerCode",
        required: true
    },
    {
        label: "table.ownerName",
        type: "input-text",
        name: "ownerName",
        required: true
    },
    {
        label: "table.ownerType",
        type: "select",
        name: "ownerType",
        source: "${OwnerType}",
        required: true
    },
    {
        label: "table.warehouseCode",
        name: "warehouseCode",
        type: "hidden",
        value: warehouseCode
    },

    // 地址信息
    {
        label: "table.country",
        type: "select",
        name: "addressDTO.country",
        source: country
    },
    {
        label: "table.province",
        type: "select",
        name: "addressDTO.province",
        initFetchOn: "data.addressDTO.country",
        source: {
            ...province,
            url: province.url + "&country-op=eq&country=${addressDTO.country}"
        }
    },
    {
        label: "table.city",
        type: "select",
        name: "addressDTO.city",
        initFetchOn: "data.addressDTO.province",
        source: {
            ...city,
            url:
                city.url +
                "&country-op=eq&country=${addressDTO.country}&province-op=eq&province=${addressDTO.province}"
        }
    },
    {
        label: "table.district/county",
        type: "select",
        name: "addressDTO.district",
        initFetchOn: "data.addressDTO.city",
        source: {
            ...district,
            url:
                district.url +
                "&country-op=eq&country=${addressDTO.country}&province-op=eq&province=${addressDTO.province}&city-op=eq&city=${addressDTO.city}"
        }
    },
    {
        label: "table.fullAddress",
        type: "input-text",
        name: "addressDTO.address"
    },

    // 联系方式
    {
        label: "table.contact",
        type: "input-text",
        name: "contactorDTO.name"
    },
    {
        label: "table.phoneNumber",
        type: "input-text",
        name: "contactorDTO.tel",
        validations: {
            isTelNumber: true
        }
    },
    {
        label: "table.email",
        type: "input-text",
        name: "contactorDTO.mail",
        validations: {
            isEmail: true
        }
    },
    {
        label: "table.fax",
        type: "input-text",
        name: "contactorDTO.fax",
        validations: {
            isTelNumber: true
        }
    }
]

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    drawer: {
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_owner_add,
            body: form
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
        name: "ownerCode",
        label: "skuArea.ownerCode",
        searchable: true
    },
    {
        name: "ownerName",
        label: "table.ownerName",
        searchable: true
    },
    {
        name: "ownerType",
        label: "table.ownerType",
        type: "mapping",
        source: "${OwnerType}",
        searchable: {
            type: "select",
            source: "${OwnerType}"
        }
    },
    {
        name: "country",
        label: "table.country"
    },
    {
        name: "province",
        label: "table.province"
    },
    {
        name: "city",
        label: "table.city"
    },
    {
        name: "district",
        label: "table.district/county"
    },
    {
        name: "name",
        label: "table.contact"
    },
    {
        name: "tel",
        label: "table.phoneNumber"
    },
    {
        name: "mail",
        label: "table.email"
    },
    {
        name: "fax",
        label: "table.fax"
    },
    ...create_update_columns
]

const searchIdentity = "MOwnerMainData"
const showColumns = columns

const schema = {
    type: "page",
    title: "shipperManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "role",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [add],
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
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    initApi: api_owner_get,
                                    api: api_owner_update,
                                    body: form
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
