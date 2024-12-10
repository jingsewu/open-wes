import schema2component from "@/utils/schema2component"

let warehouseCode = localStorage.getItem("warehouseCode")

const columns = [
    {
        name: "acceptOrderDetailId",
        dbField: "ad.id",
        hidden: true
    },
    {
        name: "acceptOrderId",
        dbField: "a.id",
        hidden: true
    },
    {
        name: "orderNo",
        dbField: "a.orderNo",
        label: "收货单号",
        searchable: true
    },
    {
        name: "identifyNo",
        dbField: "a.identifyNo",
        label: "容器",
        searchable: true
    },
    {
        name: "targetContainerSpecCode",
        dbField: "ad.targetContainerSpecCode",
        label: "容器规格"
    },
    {
        name: "targetContainerSlotCode",
        dbField: "ad.targetContainerSlotCode",
        label: "容器格口"
    },
    {
        name: "ownerCode",
        dbField: "ad.ownerCode",
        label: "货主",
        searchable: true
    },

    {
        name: "skuCode",
        dbField: "ad.sku_code",
        label: "商品编码",
        searchable: true
    },
    {
        name: "skuName",
        dbField: "ad.sku_name",
        label: "商品名称",
        searchable: true
    },
    {
        name: "qtyAccepted",
        dbField: "ad.qtyAccepted",
        label: "收货数量"
    },
    {
        name: "acceptOrderStatus",
        dbField: "a.acceptOrderStatus",
        label: "收货单状态"
    }
]

const searchIdentity = "WReceiveDTaskDetail"
const schema = {
    type: "page",
    // title: "订单详情",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            name: "ReceiveOrderDetailTable",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: columns,
                searchObject: {
                    tables: "w_accept_order a inner join w_accept_order_detail ad on a.id = ad.accept_order_id",
                    where: "a.accept_order_status = 'NEW'"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: false
            },
            headerToolbar: ["reload"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 130,
                    buttons: [
                        {
                            label: "button.close",
                            type: "button",
                            level: "link",
                            disabledOn: "${acceptOrderStatus !== 'NEW'}",
                            actionType: "dialog",
                            dialog: {
                                title: "toast.prompt",
                                body: "toast.sureCancelAccept",
                                actions: [
                                    {
                                        label: "button.cancel",
                                        actionType: "cancel",
                                        type: "button"
                                    },
                                    {
                                        label: "button.confirm",
                                        actionType: "ajax",
                                        primary: true,
                                        type: "button",
                                        api: {
                                            method: "post",
                                            url: "/wms/inbound/accept/cancel?acceptOrderId=${acceptOrderId}&acceptOrderDetailId=${acceptOrderDetailId}"
                                        },
                                        close: true,
                                        reload: "ReceiveOrderDetailTable"
                                    }
                                ]
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
