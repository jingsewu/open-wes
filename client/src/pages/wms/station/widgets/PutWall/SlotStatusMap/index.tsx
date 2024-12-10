import React from "react"
import type { PutWallTagConfigDTO } from "@/pages/wms/station/event-loop/types"

enum SlotColor {
    RED = "#f87171",
    BLUE = "#60a5fa",
    GREEN = "#34d399",
    GRAY = "#d1d5db"
}

const SlotStatusMap = (props: {
    putWallTagConfigDTO: PutWallTagConfigDTO
    putWallStatusTextMap: any
}) => {
    const { putWallTagConfigDTO, putWallStatusTextMap } = props
    return (
        <div className="d-flex">
            {Object.entries(putWallStatusTextMap).map(([key, value], index) => (
                <div
                    key={index}
                    className="d-flex items-center mr-6 text-secondary"
                >
                    <span
                        className={`w-3 h-3 mr-3 border-1  border-solid	rounded-md ${
                            putWallTagConfigDTO[key]?.mode === "FLASH"
                                ? "seeding-flash"
                                : ""
                        }`}
                        style={{
                            backgroundColor:
                                SlotColor[
                                    putWallTagConfigDTO[key]
                                        ?.color as keyof typeof SlotColor
                                ],
                            borderColor:
                                SlotColor[
                                    putWallTagConfigDTO[key]
                                        ?.color as keyof typeof SlotColor
                                ]
                        }}
                    />
                    {value}
                </div>
            ))}
        </div>
    )
}

export default SlotStatusMap
