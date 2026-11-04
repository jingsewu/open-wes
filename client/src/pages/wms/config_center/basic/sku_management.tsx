import schema2component from "@/utils/schema2component"
import { owner_code } from "@/pages/wms/constants/select_search_api_contant"
import {
    api_sku_add,
    api_sku_get,
    api_sku_update
} from "@/pages/wms/config_center/constants/api_constant"
import { enable_options } from "@/utils/commonContants"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    requiredField
} from "@/utils/amis/presets"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

const warehouseCode = localStorage.getItem("warehouseCode")

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
                        ...requiredField("validation.sku.codeRequired"),
                        disabledOn: "this.id != null"
                    },
                    {
                        type: "input-text",
                        name: "skuName",
                        label: "skuArea.productName",
                        ...requiredField("validation.sku.nameRequired")
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
                        ...requiredField("validation.sku.ownerRequired"),
                        disabledOn: "this.id != null"
                    },
                    {
                        label: "table.warehouseCode",
                        name: "warehouseCode",
                        type: "hidden",
                        value: warehouseCode
                    },

                    // 体积信息
                    {
                        label: "table.volumeMm3",
                        type: "input-number",
                        name: "volumeDTO.volume"
                    },
                    {
                        label: "table.lengthMm",
                        type: "input-number",
                        name: "volumeDTO.length"
                    },
                    {
                        label: "table.widthMm",
                        type: "input-number",
                        name: "volumeDTO.width"
                    },
                    {
                        label: "table.heightMm",
                        type: "input-number",
                        name: "volumeDTO.height"
                    },

                    // 重量信息
                    {
                        label: "table.grossWeightMg",
                        type: "input-number",
                        name: "weight.grossWeight"
                    },
                    {
                        label: "table.netWeightMg",
                        type: "input-number",
                        name: "weight.netWeight"
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
                        source: "${dictionary.SkuFirstCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuSecondCategory",
                        label: "table.levelTwoClassification",
                        source: "${dictionary.SkuSecondCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuThirdCategory",
                        label: "table.levelThreeClassification",
                        source: "${dictionary.SkuThirdCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuAttributeCategory",
                        label: "table.SKUAttributeCategories",
                        source: "${dictionary.SkuAttributeCategory}"
                    },
                    {
                        type: "select",
                        name: "skuAttribute.skuAttributeSubCategory",
                        label: "table.SKUAttributeSubcategory",
                        source: "${dictionary.SkuAttributeSubCategory}"
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
                        label: "table.shelfLifeDays"
                    },
                    {
                        type: "input-text",
                        name: "skuConfig.effectiveDays",
                        label: "table.effectiveDaysWithUnit"
                    },
                    {
                        type: "select",
                        name: "skuConfig.barcodeRuleCode",
                        label: "table.barcodeParsingRules"
                    },
                    {
                        type: "select",
                        name: "skuConfig.heat",
                        source: "${dictionary.SkuHeat}",
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

const addDrawer = formDrawer({
    title: "SKUManagement.addTitle",
    size: "lg",
    columnCount: 1,
    body: form,
    api: api_sku_add,
    reload: "SkuTable"
})

const editDrawer = formDrawer({
    title: "SKUManagement.editTitle",
    size: "lg",
    columnCount: 1,
    context: "${skuCode} · ${skuName}",
    body: form,
    initApi: api_sku_get,
    api: api_sku_update,
    reload: "SkuTable"
})

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
        ...codeColumn("ownerCode", "table.productOwner", {
            searchable: {
                type: "select",
                source: owner_code
            }
        }),
        type: "mapping",
        source: owner_code
    },
    codeColumn("skuCode", "skuArea.skuCode", { searchable: true }),
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
        source: "${dictionary.SkuFirstCategory}"
    },
    {
        name: "skuSecondCategory",
        label: "table.levelTwoClassification",
        type: "mapping",
        source: "${dictionary.SkuSecondCategory}"
    },
    {
        name: "skuThirdCategory",
        label: "table.levelThreeClassification",
        type: "mapping",
        source: "${dictionary.SkuThirdCategory}"
    },
    {
        name: "skuAttributeCategory",
        label: "table.first-level_attributes",
        type: "mapping",
        source: "${dictionary.SkuAttributeCategory}"
    },
    {
        name: "skuAttributeSubCategory",
        label: "table.second-level_attributes",
        type: "mapping",
        source: "${dictionary.SkuAttributeSubCategory}"
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "SKUManagement.title",
    name: "SkuTable",
    searchIdentity: "MSkuMainData",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "SkuTable", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.sku"
})

export default schema2component(schema)
