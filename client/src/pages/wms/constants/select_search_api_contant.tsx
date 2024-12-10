let warehouseCode = localStorage.getItem("warehouseCode")

export const country = {
    method: "post",
    url: "/search/search/searchSelectResult?perPage=1000&activePage=1",
    data: {
        searchIdentity: "SearchAddressCountry",
        searchObject: {
            tables: "m_address",
            groupBy: "country"
        },
        showColumns: [
            {
                dbField: "country",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "country",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const province = {
    method: "post",
    url: "/search/search/searchSelectResult?perPage=1000&activePage=1",
    data: {
        searchIdentity: "SearchAddressProvince",
        searchObject: {
            tables: "m_address",
            groupBy: "country, province"
        },
        showColumns: [
            {
                dbField: "province",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "province",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "country",
                name: "country",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const city = {
    method: "post",
    url: "/search/search/searchSelectResult?perPage=1000&activePage=1",
    data: {
        searchIdentity: "SearchAddressCity",
        searchObject: {
            tables: "m_address",
            groupBy: "country, province, city"
        },
        showColumns: [
            {
                dbField: "city",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "city",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "country",
                name: "country",
                javaType: "java.lang.String"
            },
            {
                dbField: "province",
                name: "province",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const district = {
    method: "post",
    url: "/search/search/searchSelectResult?perPage=1000&activePage=1",
    data: {
        searchIdentity: "SearchAddressDistrict",
        searchObject: {
            tables: "m_address",
            groupBy: "country, province, city, district"
        },
        showColumns: [
            {
                dbField: "district",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "district",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "country",
                name: "country",
                javaType: "java.lang.String"
            },
            {
                dbField: "province",
                name: "province",
                javaType: "java.lang.String"
            },
            {
                dbField: "city",
                name: "city",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const owner_code = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchOwnerCode",
        searchObject: {
            tables: "m_owner_main_data"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "owner_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "owner_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const owner_code_barcode_analysis = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchOwnerCode",
        searchObject: {
            tables: "m_owner_main_data"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "owner_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "owner_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    },
    responseData: {
        options: "${CONCAT([{value: '*',label: 'ALL'}],options)}"
    }
}

export const owner_code_by_sku_code = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&selectExclude=skuCode,warehouseCode&skuCode-op=eq&skuCode=${skuCode}&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchOwnerCodeBySkuCode",
        searchObject: {
            tables: "m_owner_main_data oma left join m_sku_main_data sma on sma.owner_code = oma.owner_code",
            distinct: true
        },
        showColumns: [
            {
                dbField: "oma.owner_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "oma.owner_name",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "sma.sku_code",
                name: "skuCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "sma.warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            }
        ]
    },
    sendOn: "this.skuCode"
}

export const warehouse_area_group = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWarehouseAreaGroupCode",
        searchObject: {
            tables: "w_warehouse_area_group"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_group_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_group_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const warehouse_area_code = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWarehouseAreaCode",
        searchObject: {
            tables: "w_warehouse_area"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const warehouse_area_id = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWarehouseAreaId",
        searchObject: {
            tables: "w_warehouse_area"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_work_type",
                name: "warehouseAreaWorkType",
                javaType: "java.lang.String"
            },
            {
                dbField: "id",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_area_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const warehouse_logic_id = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWarehouseLogicId",
        searchObject: {
            tables: "w_warehouse_logic"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "id",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_logic_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const warehouse_logic_code = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWarehouseLogicCode",
        searchObject: {
            tables: "w_warehouse_logic"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_logic_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "warehouse_logic_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const container_spec = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchContainerSpecCode",
        searchObject: {
            tables: "w_container_spec"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_type",
                name: "containerType",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_spec_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_spec_name",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_slot_specs",
                name: "containerSlotSpecs",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const put_wall_spec = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchPutWallSpecCode",
        searchObject: {
            tables: "w_container_spec",
            where: "container_type = 'PUT_WALL'"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_spec_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_slot_specs",
                name: "containerSlotSpecs",
                javaType: "java.lang.String"
            },
            {
                dbField: "container_spec_name",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const work_location = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchEmsLocationConfig",
        searchObject: {
            tables: "e_ems_location_config"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "location_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "location_code",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const work_station = {
    method: "post",
    url:
        "/search/search/searchSelectResult?perPage=1000&activePage=1&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchWorksStation",
        searchObject: {
            tables: "w_work_station",
            orderBy: "convert(station_code, UNSIGNED)"
        },
        showColumns: [
            {
                dbField: "warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "id",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "station_code",
                name: "label",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const shelf_code_table = {
    method: "post",
    url:
        "/search/search?page=${page}&perPage=10&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchContainerCodeTable",
        searchObject: {
            tables: "w_container c, w_container_spec cs",
            where: "c.container_spec_code = cs.container_spec_code and cs.container_type = 'CONTAINER'"
        },
        showColumns: [
            {
                dbField: "c.warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.container_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.container_code",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.location_code",
                name: "locationCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.warehouse_area_id",
                name: "warehouseAreaId",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.warehouse_logic_code",
                name: "warehouseLogicCode",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const container_code_table = {
    method: "post",
    url:
        "/search/search?page=${page}&perPage=10&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchContainerCodeTable",
        searchObject: {
            tables: "w_container c, w_container_spec cs",
            where: "c.container_spec_code = cs.container_spec_code and cs.container_type = 'CONTAINER'"
        },
        showColumns: [
            {
                dbField: "c.warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.container_code",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.container_code",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.location_code",
                name: "locationCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.warehouse_area_id",
                name: "warehouseAreaId",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.warehouse_logic_code",
                name: "warehouseLogicCode",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const stock_sku_id_table_columns = [
    {
        dbField: "k.sku_id",
        name: "value",
        hidden: true,
        javaType: "java.lang.Long"
    },
    {
        dbField: "max(a.sku_code)",
        name: "label",
        label: "skuArea.skuCode",
        javaType: "java.lang.String"
    },
    {
        dbField: "max(a.sku_name)",
        name: "skuCode",
        label: "skuArea.productName",
        javaType: "java.lang.String"
    },
    {
        dbField: "max(c.warehouse_area_id)",
        name: "warehouseAreaId",
        javaType: "java.lang.String",
        hidden: true
    },
    {
        dbField: "max(a.owner_code)",
        name: "ownerCode",
        label: "skuArea.ownerCode",
        javaType: "java.lang.String"
    },
    {
        dbField: "max(a.brand)",
        name: "brand",
        label: "table.brand",
        javaType: "java.lang.String"
    },
    {
        dbField: "max(a.style)",
        name: "style",
        label: "table.style",
        javaType: "java.lang.String"
    },
    {
        dbField: "max(a.color)",
        name: "color",
        label: "table.color",
        javaType: "java.lang.String"
    },
    {
        dbField: "sum(k.available_qty)",
        name: "availableQuantity",
        label: "table.availableQuantity",
        javaType: "java.lang.String"
    },
    {
        dbField: "sum(k.total_qty)",
        name: "totalQty",
        label: "table.inventoryQuantity",
        javaType: "java.lang.String"
    }
]

export const stock_sku_id_table = {
    method: "post",
    url:
        "/search/search?page=${page}&perPage=10&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchSkuIdTable",
        searchObject: {
            tables: "w_container_stock k, w_container c, m_sku_main_data a",
            where: "k.sku_id = a.id and k.container_id = c.id",
            groupBy: "k.sku_id, c.warehouse_area_id"
        },
        showColumns: stock_sku_id_table_columns
    },
    responseData: {
        options: "${items}"
    }
}

export const stock_abnormal_table = {
    method: "post",
    url:
        "/search/searchV2?page=1&perPage=500&createTime-op=bt&warehouseCode-op=eq&warehouseCode-eq=" +
        warehouseCode,
    data: {
        searchIdentity: "stocktakeByStockAbnormal"
    }
}

export const stock_sku_code_table = {
    method: "post",
    url:
        "/search/search?page=1&perPage=10&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "SearchSkuCodeTable",
        searchObject: {
            tables: "w_container_stock k, w_warehouse_ware c, m_sku_main_data a",
            where: "k.sku_id = a.id and k.container_id = c.id",
            groupBy: "k.sku_id, c.warehouse_area_id"
        },
        showColumns: [
            {
                dbField: "max(c.warehouse_code)",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "k.sku_id",
                name: "value",
                javaType: "java.lang.Long"
            },
            {
                dbField: "max(a.sku_code)",
                name: "label",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(a.sku_name)",
                name: "skuCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(c.warehouse_area_id)",
                name: "warehouseAreaId",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(a.owner_code)",
                name: "ownerCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(a.brand)",
                name: "brand",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(a.style)",
                name: "style",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(a.color)",
                name: "color",
                javaType: "java.lang.String"
            }
        ]
    }
}

export const available_stock_sku_code_table = {
    method: "post",
    url:
        "/search/search?page=1&perPage=500&selectExclude=barCode&warehouseCode-op=eq&warehouseCode=" +
        warehouseCode,
    data: {
        searchIdentity: "AvailableStockSkuCodeTable",
        searchObject: {
            tables: "w_sku_batch_stock a LEFT JOIN m_sku_main_data b ON a.sku_id = b.id LEFT JOIN m_sku_barcode_data c ON a.sku_id = c.sku_id LEFT JOIN (SELECT sku_code, GROUP_CONCAT(bar_code) bar_codes FROM m_sku_barcode_data GROUP BY sku_code) d ON b.sku_code = d.sku_code",
            where: "a.available_qty > 0",
            groupBy:
                "a.warehouse_code, b.owner_code, a.warehouse_area_id, b.sku_code, c.bar_code"
        },
        showColumns: [
            {
                dbField: "CONCAT(b.owner_code, '-', c.bar_code)",
                name: "value",
                javaType: "java.lang.String"
            },
            {
                dbField: "a.warehouse_code",
                name: "warehouseCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "b.owner_code",
                name: "ownerCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "a.warehouse_area_id",
                name: "warehouseAreaId",
                javaType: "java.lang.Long"
            },
            {
                dbField: "b.sku_code",
                name: "skuCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(b.sku_name)",
                name: "skuName",
                javaType: "java.lang.String"
            },
            {
                dbField: "c.bar_code",
                name: "barCode",
                javaType: "java.lang.String"
            },
            {
                dbField: "max(d.bar_codes)",
                name: "barCodeList",
                javaType: "java.lang.String"
            },
            {
                dbField: "sum(a.total_qty)",
                name: "totalQty",
                javaType: "java.lang.Integer"
            }
        ]
    }
}

export const stock_id_table = {
    method: "post",
    url:
        "/search/searchV2?page=1&perPage=500&warehouseCode-eq=" + warehouseCode,
    data: {
        searchIdentity: "stocktakeByStock"
    }
}
