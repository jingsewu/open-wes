import { Typography } from "antd"
import { useTranslation } from "react-i18next"

import type { Ref } from "react"
import React, { memo } from "react"
import type { CommonDescription } from "@/pages/wms/station/widgets/types"
// import IntlMessages from "@/util/IntlMessages"
import type { pickingViewItem } from "@/pages/wms/station/event-loop/types"

const { Text } = Typography
// 占位图片
export const placeholderImage =
    "data:image/png;base64,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"

interface SnInfo {
    /** sn号 */
    snCode: string
    /** sn数量 */
    snCount: number
    /** skuId */
    skuId: string
}

export interface SkuInfoProps {
    refs?: Ref<unknown>
    /** sku编码 */
    skuCode?: string
    /** 订单id */
    orderId?: string
    /** sku名称 */
    skuName: string
    /** sn详情 */
    snInfos?: SnInfo[]
    /** 批次属性 */
    batchAttributeJson?: Record<string, string>
    /** sku描述 */
    skuDesc?: CommonDescription[]
    /** 商品批次信息 */
    batchAttributesInfo?: CommonDescription[]
    /** 需要拣选数量 */
    requiredQty?: number
    /** 已经拣选数量 */
    pickedQty?: number
    /** 待拣选数量 */
    toBePickedQty?: number
    /** 数量展示规则 */
    amountDisplayRule?: any
    /** 商品图片 */
    imageUrl?: string
    /** 商品图片 */
    skuUrl?: string
    /** sku是否已经扫描过 */
    scanned: boolean
    /** 是否禁用 */
    disabled?: boolean
    /** 商品图片宽度 */
    imgWidth?: number
    /** 商品图片高度 */
    detailHeight?: number
    /** 理库任务单号 */
    taskDetailNo?: string
    /** 需要理库数量 */
    targetQty?: number
    /** 实际理库数量 */
    actualQty?: number
    /** 工作站配置的商品展示字段 */
    skuDisplayProperty?: CommonDescription[]
    skuAttributes?: any
    barCode?: string
    url: string
}

const SkuInfo = (props: Partial<pickingViewItem> & Partial<SkuInfoProps>) => {
    const {
        imgWidth = 204,
        detailHeight = 204,
        // skuMainDataDTO,
        // skuBatchAttributeDTO
        skuAttributes,
        skuName,
        barCode,
        url
    } = props

    const { t } = useTranslation()
    // const { skuAttributes = {} } = skuBatchAttributeDTO || {}
    // const { skuName, skuBarcode, skuAttribute } = skuMainDataDTO || {}
    // const url = skuAttribute?.imageUrl || placeholderImage

    const returnNotBatchPropertyContent = (value: string) => {
        return (
            <Text className="flex-1" ellipsis={{ tooltip: true }}>
                {value}
            </Text>
        )
    }

    return (
        <div className="d-flex bg-white w-full p-3 pl-0">
            <div
                className="h-full"
                style={{
                    backgroundImage: `url(${url || placeholderImage})`,
                    backgroundRepeat: "no-repeat",
                    backgroundPosition: "top center",
                    backgroundOrigin: "50%",
                    backgroundSize: "cover",
                    width: imgWidth,
                    height: detailHeight,
                    flexBasis: imgWidth,
                    backgroundColor: "#f8f8f8",
                    borderRadius: 10
                }}
            />

            <div className="d-flex flex-1 flex-col h-full pl-4 overflow-y-auto text-current  ">
                <div
                    className="text-ellipsis text-lg pb-1 w-full"
                    style={{ color: "#292B3E", minWidth: 0 }}
                    data-testid="skuCode"
                >
                    {/* {skuBarcode?.barcodes.join(" | ")} */}
                    {barCode}
                </div>
                <div
                    className="text-ellipsis text-lg pb-2 w-full"
                    style={{ color: "#292B3E", minWidth: 0 }}
                >
                    {skuName}
                </div>

                <div className="d-flex flex-wrap w-full gap-1">
                    {skuAttributes &&
                        Object.keys(skuAttributes)?.map((key, index) => {
                            return (
                                <div
                                    key={key}
                                    className="d-flex items-center  py-2 pl-3 mb-2 mr-3 "
                                    style={{
                                        color: "#696B7D",
                                        backgroundColor: "#F8F8F8",
                                        width: "45%"
                                    }}
                                >
                                    <div className="overflow-hidden text-right	white-space-nowrap text-ellipsis">
                                        <span>{key}</span>
                                        <span
                                            className="px-2"
                                            style={{ color: "#DCDCDC" }}
                                        >
                                            |
                                        </span>
                                    </div>
                                    <div>
                                        {returnNotBatchPropertyContent(
                                            skuAttributes[key]
                                        )}
                                    </div>
                                </div>
                            )
                        })}
                </div>
            </div>
        </div>
    )
}

export default memo(SkuInfo)
