import React from "react"
import { placeholderImage } from "@/pages/wms/station/widgets/common/SkuInfo"

const SKUDetail = (props: any) => {
    const {
        barcodes,
        skuCode,
        skuName,
        toBeOperatedQty,
        imageUrl,
        imgWidth = 160,
        detailHeight = 160
    } = props
    return (
        <>
            <div className="text-right">PID: {skuCode}</div>
            <div className="d-flex justify-between  my-1">
                <div
                    className="h-full"
                    style={{
                        backgroundImage: `url(${imageUrl || placeholderImage})`,
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
                <div className="w-1/2 d-flex flex-col justify-between items-center">
                    <div className="text-7xl">{toBeOperatedQty}</div>
                    <div className="text-lg font-semibold">{barcodes}</div>
                </div>
            </div>
            <div className="text-ellipsis-line2">{skuName}</div>
        </>
    )
}

export default SKUDetail
