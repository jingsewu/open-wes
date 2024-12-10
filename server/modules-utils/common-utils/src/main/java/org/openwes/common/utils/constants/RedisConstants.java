package org.openwes.common.utils.constants;

public class RedisConstants {


    private RedisConstants() {
        throw new IllegalStateException("RedisConstants class");
    }

    /**
     * topic
     */
    public static final String STATION_LISTEN_WORK_STATION_CONFIG_UPDATE = "station:listen:work:station:config:update";
    public static final String STATION_LISTEN_ORDER_ASSIGNED = "station:listen:order:assigned";
    public static final String STATION_LISTEN_CONTAINER_ARRIVED = "station:listen:container:arrived";

    public static final String STATION_LISTEN_REMIND_TO_SEAL_CONTAINER = "station:listen:remind:to:seal:container";

    public static final String STATION_LISTEN_STATION_WEBSOCKET = "station.listen.station.websocket";

    public static final String PLUGIN_LISTEN_PLUGIN_MANAGEMENT = "plugin:listen:plugin:management";

    /**
     * distributed lock
     */
    public static final String CALLBACK_SCHEDULE_EXECUTE_LOCK = "distributed:execute:lock:callback_scheduler";
    public static final String PICKING_ORDER_REALLOCATE_LOCK = "distributed:execute:lock:operation_task_reallocate";
    public static final String OUTBOUND_WAVE_SCHEDULE_EXECUTE_LOCK = "distributed:execute:lock:outbound_wave";
    public static final String PICKING_ORDER_DISPATCH_SCHEDULE_EXECUTE_LOCK = "distributed:execute:lock:picking_order_dispatch";
    public static final String TRANSFER_CONTAINER_UNLOCK_SCHEDULE_EXECUTE_LOCK = "distributed:execute:lock:transfer_container_unlock";

    /**
     * Common module data
     */
    public static final String COMMON_IMPORT_TEMPLATE_FILE = "common:import:template:";

    /**
     * Station module
     */
    public static final String WORK_STATION_OPERATE_SYNC_LOCK = "station:work:station:operate:sync:lock";

    public static final String BARCODE_PARSE_RULE_ADD_LOCK = "mdm:barcode:parse:rule:add:lock";

    public static final String BATCH_ATTRIBUTE_CONFIG_ADD_LOCK = "mdm:batch:attribute:config:add:lock";

    public static final String SKU_MAIN_DATA_CACHE = "mdm:sku:main:data:cache";
    public static final String SKU_MAIN_DATA_BARCODE_CACHE = "mdm:sku:main:data:barcode:cache";
    public static final String OWNER_MAIN_DATA_CACHE = "mdm:owner:main:data:cache";
    public static final String WAREHOUSE_MAIN_DATA_CACHE = "mdm:warehouse:main:data:cache";

    public static final String BARCODE_PARSE_RULE_CACHE = "mdm:barcode:parse:rule:cache";
    public static final String BATCH_ATTRIBUTE_CONFIG_CACHE = "mdm:batch:attribute:config:cache";
    public static final String DICTIONARY_CACHE = "mdm:dictionary:cache";

    public static final String PARAMETER_CONFIG_CACHE = "mdm:parameter:config:cache";
    public static final String SYSTEM_CONFIG_CACHE = "mdm:system:config:cache";


    /**
     * Inbound module lock
     */
    public static final String INBOUND_PLAN_ORDER_ADD_LOCK = "wms:inbound:plan:order:add:lock";
    public static final String INBOUND_ACCEPT_ADD_LOCK = "wms:inbound:accept:order:add:lock";

    /**
     * Outbound module lock
     */
    public static final String OUTBOUND_PLAN_ORDER_ADD_LOCK = "wms:outbound:plan:order:add:lock";

    public static final String OUTBOUND_PLAN_ORDER_ASSIGNED_IDS = "wms:outbound:plan:order:assigned:ids";

    public static final String NEW_PICKING_ORDER_IDS = "wms:picking:order:ids";

    public static final String PICKING_ORDER_ABNORMAL_DETAIL_IDS = "wms:picking:order:detail:ids";

    public static final String RECEIVING_MANUAL_PICKING_USER_LOCK = "wms:picking:manual:receiving:user:lock";

    public static final String RECEIVING_MANUAL_PICKING_ORDER_LOCK = "wms:picking:manual:receiving:order:lock";

    public static final String RECEIVING_MANUAL_PICKING_WAVE_LOCK = "wms:picking:manual:receiving:wave:lock";

    /**
     * Stock module lock
     */
    public static final String STOCKTAKE_CREATE_RECORD_LOCK = "wms:stock:take:create:record:lock";
    public static final String STOCK_GET_CREATE_SKU_BATCH_ATTR_LOCK = "wms:stock:get:create:sku:batch:attr:lock";
    public static final String STOCK_SYNC_LOCK = "wms:stock:sync:lock";

    /**
     * Basic Module
     */
    public static final String WORK_STATION_CACHE = "wms:basic:work:station:cache";
    public static final String WORK_STATION_CONFIG_CACHE = "station:work:config:cache";
    public static final String WORK_STATION_PUT_WALL_CACHE = "station:work:put:wall:cache";
    public static final String WORK_STATION_PUT_WALL_SLOT_CACHE = "station:work:put:wall:cache";

    /**
     * Api-platform module
     */
    public static final String API_CACHE = "api:platform:api:cache";
    public static final String API_CONFIG_CACHE = "api:platform:api:config:cache";


    /**
     * Tenant module
     */
    public static final String TENANT_CONFIG_CACHE = "tenant:tenant:config:cache";

    /**
     * User module
     */
    public static final String USER_TOKEN_CACHE = "user:token:cache:";

    /**
     * PAD接收SKU缓存
     */
    public static final String PAD_RECEIVE_SKU_CACHE = "pad:receive:sku:cache:";
}
