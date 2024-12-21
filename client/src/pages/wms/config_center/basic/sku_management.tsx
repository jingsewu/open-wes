import schema2component from "@/utils/schema2component"
import { owner_code } from "@/pages/wms/constants/select_search_api_contant"
import {
    volume,
    weight
} from "@/pages/wms/config_center/constants/form_constants"
import {
    api_sku_add,
    api_sku_get,
    api_sku_update
} from "@/pages/wms/config_center/constants/api_constant"
import { create_update_columns, enable_options } from "@/utils/commonContants"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

let warehouseCode = localStorage.getItem("warehouseCode")

const form = [
    {
        type: "tabs",
        tabs: [
            {
                title: "form.tab.basicInformation",
                controls: [
                    {
                        type: "input-text",
                        name: "id",
                        label: "table.productId",
                        hidden: true
                    },
                    {
                        type: "input-text",
                        name: "skuCode",
                        label: "skuArea.skuCode",
                        required: true,
                        disabledOn: "this.id != null"
                    },
                    {
                        type: "input-text",
                        name: "skuName",
                        label: "skuArea.productName",
                        required: true
                    },
                    {
                        type: "input-text",
                        name: "style",
                        label: "table.style"
                    },
                    {
                        type: "input-text",
                        name: "color",
                        label: "table.color"
                    },
                    {
                        type: "input-text",
                        name: "size",
                        label: "table.size"
                    },
                    {
                        type: "input-text",
                        name: "brand",
                        label: "table.brand"
                    },
                    {
                        type: "select",
                        name: "ownerCode",
                        label: "table.productOwner",
                        source: owner_code,
                        required: true,
                        disabledOn: "this.id != null"
                    },
                    {
                        label: "table.warehouseCode",
                        name: "warehouseCode",
                        type: "hidden",
                        value: warehouseCode
                    },
                    // {
                    //     "type": "switch",
                    //     "name": "suit",
                    //     "label": "是否套装"
                    // },

                    // 体积信息
                    {
                        label: "table.volume",
                        type: "input-number",
                        name: "volumeDTO.volume",
                        description: "mm³"
                    },
                    {
                        label: "table.length",
                        type: "input-number",
                        name: "volumeDTO.length",
                        description: "mm"
                    },
                    {
                        label: "table.width",
                        type: "input-number",
                        name: "volumeDTO.width",
                        description: "mm"
                    },
                    {
                        label: "table.height",
                        type: "input-number",
                        name: "volumeDTO.height",
                        description: "mm"
                    },

                    // 重量信息
                    {
                        label: "table.grossWeight",
                        type: "input-number",
                        name: "weight.grossWeight",
                        description: "table.milligram(mg)"
                    },
                    {
                        label: "table.netWeight",
                        type: "input-number",
                        name: "weight.netWeight",
                        description: "table.milligram(mg)"
                    },

                    // 图片属性
                    {
                        type: "input-text",
                        name: "skuAttribute.imageUrl",
                        label: "table.pictureAddress"
                    },
                    {
                        type: "input-text",
                        name: "skuAttribute.unit",
                        label: "table.unit"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuFirstCategory",
                        label: "table.levelOneClassification",
                        source: "${ls:dictionary|pick:SkuFirstCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuSecondCategory",
                        label: "table.levelTwoClassification",
                        source: "${ls:dictionary|pick:SkuSecondCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuThirdCategory",
                        label: "table.levelThreeClassification",
                        source: "${ls:dictionary|pick:SkuThirdCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuAttributeCategory",
                        label: "table.SKUAttributeCategories",
                        source: "${ls:dictionary|pick:SkuAttributeCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuAttributeSubCategory",
                        label: "table.SKUAttributeSubcategory",
                        source: "${ls:dictionary|pick:SkuAttributeSubCategory}"
                    }
                ]
            },
            {
                title: "form.tab.configurationInformation",
                controls: [
                    // 商品配置信息
                    {
                        type: "switch",
                        name: "skuConfig.enableSn",
                        label: "table.uniqueCodeManagement",
                        options: enable_options
                    },
                    {
                        type: "switch",
                        name: "skuConfig.enableEffective",
                        label: "table.expirationDateManagement",
                        options: enable_options
                    },
                    {
                        type: "input-text",
                        name: "skuConfig.shelfLife",
                        label: "table.shelfLife",
                        description: "table.days"
                    },
                    {
                        type: "input-text",
                        name: "skuConfig.effectiveDays",
                        label: "table.expirationDate",
                        description: "table.days"
                    },
                    {
                        type: "select",
                        name: "skuConfig.barcodeRuleCode",
                        label: "table.barcodeParsingRules"
                    },
                    {
                        type: "select",
                        name: "skuConfig.heat",
                        source: "${ls:dictionary|pick:SkuHeat}",
                        label: "table.SKUHeat"
                    },
                    {
                        type: "input-number",
                        name: "skuConfig.maxCount",
                        label: "table.inventoryLimit"
                    },
                    {
                        type: "input-number",
                        name: "skuConfig.minCount",
                        label: "table.inventoryMinimum"
                    },
                    {
                        type: "switch",
                        name: "skuConfig.calculateHeat",
                        label: "table.calculateTheHeat"
                    },
                    {
                        type: "switch",
                        name: "skuConfig.noBarcode",
                        label: "table.WhetherUncodedSKU",
                        options: enable_options
                    },

                    // 商品条码信息
                    {
                        name: "skuBarcode.barcodes",
                        label: "skuArea.barcode",
                        mode: "horizontal",
                        type: "input-array",
                        inline: true,
                        removable: true,
                        items: {
                            type: "input-text",
                            clearable: false
                        }
                    }
                ]
            }
        ]
    }
]

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    drawer: {
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_sku_add,
            body: form
        }
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "warehouseCode",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    {
        name: "ownerCode",
        label: "table.productOwner",
        type: "mapping",
        source: owner_code,
        searchable: {
            type: "select",
            source: owner_code
        }
    },
    {
        name: "skuCode",
        label: "skuArea.skuCode",
        searchable: true
    },
    {
        name: "skuName",
        label: "skuArea.productName",
        searchable: true
    },
    {
        name: "style",
        label: "table.style"
    },
    {
        name: "color",
        label: "table.color"
    },
    {
        name: "size",
        label: "table.size"
    },
    {
        name: "brand",
        label: "table.brand"
    },
    {
        name: "skuFirstCategory",
        label: "table.levelOneClassification",
        type: "mapping",
        source: "${ls:dictionary|pick:SkuFirstCategory}"
    },
    {
        name: "skuSecondCategory",
        label: "table.levelTwoClassification",
        type: "mapping",
        source: "${ls:dictionary|pick:SkuSecondCategory}"
    },
    {
        name: "skuThirdCategory",
        label: "table.levelThreeClassification",
        type: "mapping",
        source: "${ls:dictionary|pick:SkuThirdCategory}"
    },
    {
        name: "skuAttributeCategory",
        label: "table.first-level_attributes",
        type: "mapping",
        source: "${ls:dictionary|pick:SkuAttributeCategory}"
    },
    {
        name: "skuAttributeSubCategory",
        label: "table.second-level_attributes",
        type: "mapping",
        source: "${ls:dictionary|pick:SkuAttributeSubCategory}"
    },
    ...create_update_columns
]

const searchIdentity = "MSkuMainData"
const showColumns = columns

const schema = {
    type: "page",
    title: "SKUManagement.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "role",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [add],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 100,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    initApi: api_sku_get,
                                    api: api_sku_update,
                                    controls: form
                                }
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
