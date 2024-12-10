import React from "react"
import { SlotColor } from "../types"
import {
    putWallSlotsItem,
    PutWallTagConfigDTO,
    PutWallSlotStatusEnum
} from "@/pages/wms/station/event-loop/types"

interface Props {
    data: putWallSlotsItem
    putWallTagConfigDTO: PutWallTagConfigDTO
    onSlotClick?: (data: putWallSlotsItem) => void
}

const SlotItem: React.FC<Props> = (props) => {
    const { data, putWallTagConfigDTO, onSlotClick } = props

    const toBePickedQty = data.putWallSlotDesc?.find(
        (item) => item.fieldName === "tobeOperatedQty"
    )?.fieldValue
    // 禁用转成状态
    const status = data.enable
        ? PutWallSlotStatusEnum[data.putWallSlotStatus]
        : PutWallSlotStatusEnum.DISABLE

    const style = {
        color:
            putWallTagConfigDTO[status] &&
            status !== PutWallSlotStatusEnum["DISABLE"]
                ? "#FFF"
                : "#333",
        backgroundColor:
            SlotColor[
                putWallTagConfigDTO[status]?.color as keyof typeof SlotColor
            ] || "#fff"
    }

    return (
        <div
            className={`relative d-flex flex-1 items-center justify-between h-full overflow-hidden border-2	text-current px-2 rounded-md data-wms ${status} ${
                putWallTagConfigDTO[status]?.mode === "FLASH"
                    ? "seeding-flash"
                    : ""
            }`}
            style={{
                ...style,
                cursor:
                    data.enable && data.putWallSlotStatus !== "DISABLE"
                        ? "pointer"
                        : "not-allowed",
                minHeight: 50
            }}
            data-testid={status}
            onClick={() => onSlotClick?.(data)}
        >
            <span>{data.putWallSlotCode}</span>
            <span>{toBePickedQty}</span>
            <span>{data.transferContainerCode}</span>
        </div>
    )
}

export default SlotItem
