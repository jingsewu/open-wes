import schema2component from "@/utils/schema2component"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

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
        dbField: "a.order_no",
        label: "table.acceptOrderNo",
        searchable: true
    },
    {
        name: "identifyNo",
        dbField: "a.identify_no",
        label: "table.containerCode",
        searchable: true
    },
    {
        name: "targetContainerSpecCode",
        dbField: "ad.target_container_spec_code",
        label: "workLocationArea.containerSpecification"
    },
    {
        name: "targetContainerSlotCode",
        dbField: "ad.target_container_slot_code",
        label: "table.containerLatticeSlogan"
    },
    {
        name: "ownerCode",
        dbField: "ad.owner_code",
        label: "skuArea.ownerCode",
        searchable: true
    },

    {
        name: "skuCode",
        dbField: "ad.sku_code",
        label: "skuArea.skuCode",
        searchable: true
    },
    {
        name: "skuName",
        dbField: "ad.sku_name",
        label: "skuArea.productName",
        searchable: true
    },
    {
        name: "qtyAccepted",
        dbField: "ad.qty_accepted",
        label: "table.receivedQuantity"
    },
    {
        name: "acceptOrderStatus",
        dbField: "a.accept_order_status",
        label: "table.receive.order.status"
    }
]

const searchIdentity = "WReceivedTaskDetail"
const schema = {
    type: "page",
    toolbar: [],
    body: [
        {
            type: "crud",
            name: "ReceiveOrderDetailTable",
            api: api_crud_search_by_warehouseCode,
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
