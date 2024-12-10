let warehouseCode = localStorage.getItem("warehouseCode")

export const stationOption = {
    method: "post",
    url:
        "/search/search?page=1&perPage1000&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "WWorkStation1",
        searchObject: {
            orderBy: "update_time desc"
        },
        showColumns: [
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
                name: "stationCode",
                label: "table.workstationCoding",
                searchable: true
            },
            {
                name: "stationName",
                label: "table.workstationName"
            }
        ]
    }
}
