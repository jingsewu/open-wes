import React, {lazy} from "react"
import {Translation} from "react-i18next"

const WorkStation = lazy(() => import("@/pages/wms/station"))

interface RouterItem {
    [param: string]: any
}

const meta = {
    keepAlive: true,
    closable: true,
    cache: true
}

const baseRouter = [
    {
        path: "/",
        component: lazy(() => import("@/pages/Login"))
    },
    {
        path: "/login",
        component: lazy(() => import("@/pages/Login"))
    },
    {
        path: "/register",
        component: lazy(() => import("@/pages/Register"))
    }
]

const menuRouter = [
    {
        path: "/user/user-manage",
        name: (
            <Translation>
                {(t) => t("userCenter.userManagement.title")}
            </Translation>
        ),
        component: lazy(() => import("@/pages/user/user_management"))
    },
    {
        path: "/user/role-manage",
        name: (
            <Translation>
                {(t) => t("userCenter.roleManagement.title")}
            </Translation>
        ),
        component: lazy(() => import("@/pages/user/role_management"))
    },
    {
        path: "/user/menu-manage",
        name: (
            <Translation>
                {(t) => t("userCenter.menuManagement.title")}
            </Translation>
        ),
        component: lazy(() => import("@/pages/user/menu_management"))
    },
    {
        path: "/user/login-log-manage",
        name: (
            <Translation>{(t) => t("userCenter.loginLogs.title")}</Translation>
        ),
        component: lazy(() => import("@/pages/user/login_log"))
    },
    {
        path: "/user/oauth-client-manage",
        name: (
            <Translation>
                {(t) => t("user.menu.clientAuthorization")}
            </Translation>
        )
    },

    // API Platform
    {
        path: "/api-platform/api-management",
        name: (
            <Translation>
                {(t) => t("interfacePlatform.interfaceManagement.title")}
            </Translation>
        ),
        component: lazy(() => import("@/pages/api_platform/api_management"))
    },
    {
        path: "/api-platform/api-log",
        name: (
            <Translation>
                {(t) => t("interfacePlatform.interfaceLogs.title")}
            </Translation>
        ),
        component: lazy(() => import("@/pages/api_platform/api_log"))
    },

    // WMS - config center - basic
    {
        path: "/wms/config-center",
        name: (
            <Translation>
                {(t) => t("wms.menu.configurationCenter")}
            </Translation>
        )
    },
    {
        path: "/wms/config-center/basicInfo-config",
        name: <Translation>{(t) => t("form.tab.basicInformation")}</Translation>
    },
    {
        path: "/wms/config-center/basic/owner-management",
        name: <Translation>{(t) => t("shipperManagement.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/config_center/basic/owner_management")
        )
    },
    {
        path: "/wms/config-center/basic/sku-management",
        name: <Translation>{(t) => t("SKUManagement.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/config_center/basic/sku_management")
        )
    },
    {
        path: "/mdm/config-center/basic/batch-attribute-management",
        name: <Translation>{(t) => t("batchManagement.title")}</Translation>,
        component: lazy(
            () =>
                import(
                    "@/pages/wms/config_center/basic/batch_attribute_management"
                    )
        )
    },
    {
        path: "/wms/config-center/basic/container-spec-management",
        name: <Translation>{(t) => t("containerSpec.title")}</Translation>,
        component: lazy(
            () =>
                import(
                    "@/pages/wms/config_center/basic/container_spec_management"
                    )
        )
    },
    {
        path: "/wms/config-center/basic/container-management",
        name: (
            <Translation>{(t) => t("containerManagement.title")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/basic/container_management")
        )
    },
    {
        path: "/wms/config-center/basic/put-wall-management",
        name: (
            <Translation>{(t) => t("seedingWallManagement.title")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/basic/put_wall_management")
        )
    },
    {
        path: "/wms/config-center/basic/work-station",
        name: (
            <Translation>{(t) => t("workstationManagement.title")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/basic/work_station")
        )
    },

    // WMS - config center - warehouse
    {
        path: "/wms/config-center/warehouse",
        name: (
            <Translation>
                {(t) => t("wms.menu.warehouseInformation")}
            </Translation>
        )
    },
    {
        path: "/wms/config-center/warehouse/warehouse-management",
        name: (
            <Translation>{(t) => t("warehouseManagement.title")}</Translation>
        ),
        component: lazy(
            () =>
                import(
                    "@/pages/wms/config_center/warehouse/warehouse_management"
                    )
        )
    },
    {
        path: "/wms/config-center/warehouse/warehouse-area-group",
        name: (
            <Translation>
                {(t) => t("warehouseAreaManagement.title")}
            </Translation>
        ),
        component: lazy(
            () =>
                import(
                    "@/pages/wms/config_center/warehouse/warehouse_area_group"
                    )
        )
    },
    {
        path: "/wms/config-center/warehouse/warehouse-area",
        name: (
            <Translation>
                {(t) => t("reservoirAreaManagement.title")}
            </Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/warehouse/warehouse_area")
        )
    },
    {
        path: "/wms/config-center/warehouse/warehouse-logic",
        name: (
            <Translation>{(t) => t("logicalAreaManagement.title")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/warehouse/warehouse_logic")
        )
    },
    {
        path: "/wms/config-center/warehouse/shelf-management",
        name: <Translation>{(t) => t("wms.menu.shelfManagement")}</Translation>
    },
    {
        path: "/wms/config-center/warehouse/location-management",
        name: <Translation>{(t) => t("locationManagement.title")}</Translation>,
        component: lazy(
            () =>
                import(
                    "@/pages/wms/config_center/warehouse/location_management"
                    )
        )
    },

    // WMS - config center - rule
    {
        path: "/wms/rule",
        name: <Translation>{(t) => t("wms.menu.rulesManagement")}</Translation>
    },
    {
        path: "/wms/config-center/rule/system-config",
        name: (
            <Translation>
                {(t) => t("systemConfigManagement.title")}
            </Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/rule/system_config")
        )
    },
    {
        path: "/wms/config-center/rule/dictionary-management",
        name: (
            <Translation>{(t) => t("dictionaryManagement.title")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/rule/dictionary_management")
        )
    },
    {
        path: "/wms/config-center/rule/number-rule",
        name: <Translation>{(t) => t("wms.menu.encodingRules")}</Translation>
    },
    {
        path: "/wms/config-center/rule/barcode-parse-rule",
        name: (
            <Translation>
                {(t) => t("barcodeParsingManagement.title")}
            </Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/config_center/rule/barcode_parse_rule")
        )
    },
    {
        path: "/wms/data-center",
        name: <Translation>{(t) => t("wms.menu.dataCenters")}</Translation>
    },
    {
        path: "/wms/data-center/inbound-manage",
        name: <Translation>{(t) => t("wms.menu.inboundManage")}</Translation>
    },

    // WMS - data center - inbound
    {
        path: "/wms/data-center/inbound/inbound-plan-order",
        name: <Translation>{(t) => t("menu.inboundOrder")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/inbound/inbound_plan_order")
        )
    },
    {
        path: "/wms/data-center/inbound/putaway-task",
        name: <Translation>{(t) => t("wms.menu.putAwayTasks")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/inbound/put_away_task")
        )
    },
    {
        path: "/wms/data-center/inbound/empty-container-inbound",
        name: (
            <Translation>
                {(t) => t("wms.menu.emptyContainerInbound")}
            </Translation>
        ),
        component: lazy(
            () =>
                import(
                    "@/pages/wms/data_center/inbound/empty_container_inbound"
                    )
        )
    },
    {
        path: "/wms/data-center/receiving-manage",
        name: (
            <Translation>{(t) => t("wms.menu.receiptManagement")}</Translation>
        ),
        component: lazy(
            () =>
                import(
                    "@/pages/wms/data_center/inbound/accept_order"
                    )
        )
    },
    {
        path: "/wms/data-center/outbound-manage",
        name: <Translation>{(t) => t("wms.menu.outboundManage")}</Translation>
    },

    // WMS - data center - outbound
    {
        path: "/wms/data-center/outbound/outbound-plan-order",
        name: <Translation>{(t) => t("outboundOrder.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/outbound/outbound_plan_order")
        )
    },
    {
        path: "/wms/data-center/empty-container-outbound",
        name: (
            <Translation>
                {(t) => t("wms.menu.emptyContainerOutbound")}
            </Translation>
        )
    },

    {
        path: "/wms/data-center/outbound/picking-order",
        name: <Translation>{(t) => t("pickingTasks.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/outbound/picking_order")
        )
    },
    {
        path: "/wms/data-center/stock/order-schedule",
        name: <Translation>{(t) => t("wms.menu.orderDispatch")}</Translation>
    },
    {
        path: "/wms/data-center/stock/stock-manage",
        name: <Translation>{(t) => t("wms.menu.inventoryManage")}</Translation>
    },

    // WMS - data center - stock
    {
        path: "/wms/data-center/stock/container-stock",
        name: <Translation>{(t) => t("inventoryDetails.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/stock/container_stock")
        )
    },
    {
        path: "/wms/data-center/stock/stock-abnormal",
        name: <Translation>{(t) => t("inventorAnomalies.title")}</Translation>,
        component: lazy(
            () => import("@/pages/wms/data_center/stock/stock_abnormal")
        )
    },
    {
        path: "/wms/data-center/stock/stock-adjust",
        name: (
            <Translation>
                {(t) => t("wms.menu.inventoryAdjustment")}
            </Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/data_center/stock/stock_adjust")
        )
    },
    {
        path: "/wms/data-center/stock/stocktake",
        name: <Translation>{(t) => t("wms.menu.inventoryCheck")}</Translation>,
        component: lazy(() => import("@/pages/wms/data_center/stock/stocktake"))
    },
    {
        path: "/wms/data-center/stock/stock-inventory",
        name: <Translation>{(t) => t("wms.menu.inventoryTally")}</Translation>
    },
    {
        path: "/wms/data-center/stock/inventory-task",
        name: <Translation>{(t) => t("wms.menu.tallyTask")}</Translation>
    },
    {
        path: "/wms/data-center/stock/stock-record",
        name: (
            <Translation>{(t) => t("wms.menu.inventoryRecords")}</Translation>
        ),
        component: lazy(
            () => import("@/pages/wms/data_center/stock/stock_records")
        )
    },
    {
        path: "/wms/data-center/stock/batch-attribute-adjust",
        name: (
            <Translation>
                {(t) => t("wms.menu.batchAttributeAdjustment")}
            </Translation>
        )
    },
    {
        path: "/wms/data-center/stock/serial-number-stock",
        name: (
            <Translation>
                {(t) => t("wms.menu.serialNumberInventory")}
            </Translation>
        )
    },
    {
        path: "/wms/data-center/stock/stock-transfer",
        name: (
            <Translation>{(t) => t("wms.menu.inventoryTransfer")}</Translation>
        )
    },
    {
        path: "/wms/data-center/stock/stock-transfer-task",
        name: (
            <Translation>
                {(t) => t("wms.menu.inventoryTransferTask")}
            </Translation>
        )
    },

    // work station system
    {
        path: "/wms/workStation",
        name: <Translation>{(t) => t("station.operatingStation")}</Translation>,
        // component: lazy(() => import("@/pages/wms/station/router"))
        component: (props: any) => <WorkStation {...props} type="card"/>,
        meta: {
            ...meta,
            cache: false
        }
    },
    {
        path: "/wms/workStation/receive",
        name: "商品收货",
        component: (props: any) => <WorkStation {...props} type="receive"/>,
        meta: {
            ...meta,
            cache: false
        }
    },
    {
        path: "/wms/workStation/outbound",
        name: "出库",
        component: (props: any) => <WorkStation {...props} type="outbound"/>,
        meta: {
            ...meta,
            cache: false
        }
    },
    {
        path: "/wms/workStation/stocktake",
        name: "盘点",
        component: (props: any) => <WorkStation {...props} type="stocktake"/>,
        meta: {
            ...meta,
            cache: false
        }
    },
    {
        path: "/wms/dashboard",
        name: <Translation>{(t) => t("wms.menu.dashboard")}</Translation>,
        component: lazy(() => import("@/pages/wms/dashboard"))
    }
]

const router = menuRouter.map((item: RouterItem) => {
    return item.meta
        ? item
        : {
            ...item,
            meta
        }
})

const path2components = [...baseRouter, ...router]

export default path2components
