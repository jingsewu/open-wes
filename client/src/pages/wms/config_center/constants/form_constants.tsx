import {
    city,
    country,
    district,
    province
} from "@/pages/wms/constants/select_search_api_contant"

export const contactor = [
    {
        label: "table.contact",
        type: "input-text",
        name: "name"
    },
    {
        label: "table.phoneNumber",
        type: "input-text",
        validations: {
            isTelNumber: true
        },
        name: "tel"
    },
    {
        label: "table.email",
        type: "input-text",
        validations: {
            isEmail: true
        },
        name: "mail"
    },
    {
        label: "table.fax",
        type: "input-text",
        validations: {
            isTelNumber: true
        },
        name: "fax"
    }
]

export const address = [
    {
        label: "table.country",
        type: "select",
        name: "country",
        source: country
    },
    {
        label: "table.province",
        type: "select",
        name: "province",
        initFetchOn: "data.country",
        source: {
            ...province,
            url: province.url + "&country-op=eq&country=${country}"
        }
    },
    {
        label: "table.city",
        type: "select",
        name: "city",
        initFetchOn: "data.province",
        source: {
            ...city,
            url:
                city.url +
                "&country-op=eq&country=${country}&province-op=eq&province=${province}"
        }
    },
    {
        label: "table.district/county",
        type: "select",
        name: "district",
        initFetchOn: "data.city",
        source: {
            ...district,
            url:
                district.url +
                "&country-op=eq&country=${country}&province-op=eq&province=${province}&city-op=eq&city=${city}"
        }
    },
    {
        label: "table.fullAddress",
        type: "input-text",
        name: "address"
    }
]

export const volume = [
    {
        label: "table.volume(mm³)",
        type: "input-number",
        name: "volume"
        // description: "mm³"
    },
    {
        label: "table.length(mm)",
        type: "input-number",
        name: "length"
        // description: "mm"
    },
    {
        label: "table.width(mm)",
        type: "input-number",
        name: "width"
        // description: "mm"
    },
    {
        label: "table.height(mm)",
        type: "input-number",
        name: "height"
        // description: "mm"
    }
]

export const weight = [
    {
        label: "table.grossWeight",
        type: "input-number",
        name: "grossWeight"
    },
    {
        label: "table.netWeight",
        type: "input-number",
        name: "netWeight"
    }
]
