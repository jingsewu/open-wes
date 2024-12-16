export const warehouseSelectApi = (warehouses: Array<string>) => {
    return {
        method: "post",
        url:
            "/search/search/searchSelectResult?perPage=1000&activePage=1&value-op=il&value=" +
            warehouses.join(","),
        data: {
            searchIdentity: "SearchWarehouseMainData",
            searchObject: {
                tables: "m_warehouse_main_data"
            },
            showColumns: [
                {
                    dbField: "warehouse_code",
                    name: "value",
                    javaType: "java.lang.String"
                },
                {
                    dbField: "warehouse_name",
                    name: "label",
                    javaType: "java.lang.String"
                }
            ]
        }
    }
}

export const api_crud_search = {
    method: "POST",
    url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt",
    dataType: "application/json"
}

export const api_crud_search_by_warehouseCode = {
    method: "POST",
    url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=${ls:warehouseCode}",
    dataType: "application/json"
}

export const api_crud_search_total = {
    method: "POST",
    url: "/search/search?page=${1}&perPage=${100000}&createTime-op=bt",
    dataType: "application/json"
}

export const api_crud_search_by_warehouseCode_total = {
    method: "POST",
    url: "/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=${ls:warehouseCode}",
    dataType: "application/json"
}
