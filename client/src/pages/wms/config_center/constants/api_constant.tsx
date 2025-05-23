// warehouse module
export const api_warehouse_add = "post:/wms/basic/warehouse/createOrUpdate"
export const api_warehouse_update = "post:/wms/basic/warehouse/createOrUpdate"
export const api_warehouse_get = "post:/wms/basic/warehouse/get/${id}"

export const api_warehouse_config_add = "post:/wms/basic/warehouse/config/createOrUpdate"
export const api_warehouse_config_get = "post:/wms/basic/warehouse/config/get/${warehouseCode}"

export const api_warehouse_area_group_add = "post:/wms/basic/warehouse/areaGroup/createOrUpdate"
export const api_warehouse_area_add = "post:/wms/basic/warehouse/area/createOrUpdate"
export const api_warehouse_area_logic_add = "post:/wms/basic/warehouse/areaLogic/createOrUpdate"

// owner module
export const api_owner_add = "post:/wms/basic/owner/createOrUpdate"
export const api_owner_update = "post:/wms/basic/owner/createOrUpdate"
export const api_owner_get = "post:/wms/basic/owner/${id}"

// sku module
export const api_sku_add = "post:/wms/basic/sku/createOrUpdate"
export const api_sku_update = "post:/wms/basic/sku/createOrUpdate"
export const api_sku_get = "post:/wms/basic/sku/${id}"

// barcode parse rule module
export const api_barcode_parse_rule_add = "post:/wms/config/barcode/parse/createOrUpdate"
export const api_barcode_parse_rule_update = "post:/wms/config/barcode/parse/createOrUpdate"
export const api_barcode_parse_rule_get = "post:/wms/config/barcode/parse/get/${id}"

// batch attribute module
export const api_batch_attribute_add = "post:/wms/config/batchAttribute/createOrUpdate"
export const api_batch_attribute_update = "post:/wms/config/batchAttribute/createOrUpdate"
export const api_batch_attribute_get = "post:/wms/config/batchAttribute/get/${id}"

// dictionary module
export const api_dictionary_add = "post:/wms/config/dictionary/createOrUpdate"
export const api_dictionary_update = "post:/wms/config/dictionary/createOrUpdate"
export const api_dictionary_get = "post:/wms/config/dictionary/get/${id}"

// system config module
export const api_system_config_save_or_update = "post:/wms/config/system/config/createOrUpdate"
export const api_system_config_get = "post:/wms/config/system/config/getSystemConfig"

// container spec module
export const api_container_spec_add = "post:/wms/basic/containerSpec/createOrUpdate"
export const api_container_spec_update = "post:/wms/basic/containerSpec/createOrUpdate"
export const api_container_spec_get = "post:/wms/basic/containerSpec/get/${id}"
export const api_container_spec_delete = "post:/wms/basic/containerSpec/delete/${id}"

// container module
export const api_container_batch_add = "post:/wms/basic/container/create"
export const api_container_change_spec = "post:/wms/basic/container/changeContainerSpec/${containerId}"
export const api_container_get = "post:/wms/basic/container/get/${id}"
export const api_container_delete = "post:/wms/basic/container/delete/${id}"

// work station module
export const api_work_station_add = "post:/wms/basic/work/station/createOrUpdateWorkStation"
export const api_work_station_get = "post:/wms/basic/work/station/get/${id}"
export const api_work_station_config_get = "/wms/basic/work/station/config/get/${id}"
export const api_work_station_config_add = "post:/wms/basic/work/station/createOrUpdateStationConfig"

// put wall module
export const api_put_wall_add = "/wms/basic/putWall/createOrUpdate"
export const api_put_wall_get = "post:/wms/basic/putWall/get/${id}"
export const api_put_wall_delete = "post:/wms/basic/putWall/delete/${id}"

// transfer container module
export const api_transfer_container_release = "post:/wms/basic/transfer/container/release/${id}"

// print module
export const api_print_config_add = "post:/wms/print/config/createOrUpdate"
export const api_print_config_get = "post:/wms/print/config/get/${id}"
export const api_print_config_delete = "post:/wms/print/config/delete/${id}"

export const api_print_rule_add = "post:/wms/print/rule/createOrUpdate"
export const api_print_rule_get = "post:/wms/print/rule/get/${id}"
export const api_print_rule_delete = "post:/wms/print/rule/delete/${id}"

export const api_print_template_add = "post:/wms/print/template/createOrUpdate"
export const api_print_template_get = "post:/wms/print/template/get/${id}"
