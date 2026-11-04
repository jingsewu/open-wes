import schema2component from "@/utils/schema2component"
import {
    api_owner_add,
    api_owner_get,
    api_owner_update
} from "@/pages/wms/config_center/constants/api_constant"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    requiredField
} from "@/utils/amis/presets"
import {
    city,
    country,
    district,
    province
} from "@/pages/wms/constants/select_search_api_contant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

const warehouseCode = localStorage.getItem("warehouseCode")

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
        ...requiredField("validation.owner.codeRequired")
    },
    {
        label: "table.ownerName",
        type: "input-text",
        name: "ownerName",
        ...requiredField("validation.owner.nameRequired")
    },
    {
        label: "table.ownerType",
        type: "select",
        name: "ownerType",
        source: "${dictionary.OwnerType}",
        ...requiredField("validation.owner.typeRequired")
    },
    {
        label: "table.warehouseCode",
        name: "warehouseCode",
        type: "hidden",
        value: warehouseCode
    },
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
        label: "table.districtCounty",
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

const addDrawer = formDrawer({
    title: "shipperManagement.addTitle",
    body: form,
    api: api_owner_add,
    reload: "OwnerTable"
})

const editDrawer = formDrawer({
    title: "shipperManagement.editTitle",
    context: "${ownerCode} · ${ownerName}",
    body: form,
    initApi: api_owner_get,
    api: api_owner_update,
    reload: "OwnerTable"
})

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
    codeColumn("ownerCode", "skuArea.ownerCode", {
        searchable: true
    }),
    {
        name: "ownerName",
        label: "table.ownerName",
        searchable: true
    },
    {
        name: "ownerType",
        label: "table.ownerType",
        type: "mapping",
        source: "${dictionary.OwnerType}",
        searchable: {
            type: "select",
            source: "${dictionary.OwnerType}"
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
        label: "table.districtCounty"
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
    ...auditColumns()
]

const schema = crudPage({
    title: "shipperManagement.title",
    name: "OwnerTable",
    searchIdentity: "MOwnerMainData",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [
        addButton({
            target: "OwnerTable",
            drawer: addDrawer
        })
    ],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.shipper"
})

export default schema2component(schema)
