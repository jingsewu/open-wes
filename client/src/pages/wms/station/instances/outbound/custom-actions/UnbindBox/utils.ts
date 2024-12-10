import type { putWallViewsItem } from "@/pages/wms/station/event-loop/types"
/**
 * 当前播种墙数据适配函数
 * @param putWallViews
 */
export const viewsDataAdaptor = (putWallViews: putWallViewsItem[]) => {
    return putWallViews.map((item) => {
        return {
            ...item,
            putWallSlots: item.putWallSlots.map((slot) => {
                // 处理槽口状态
                return {
                    ...slot,
                    // IDLE + 箱号 = 此箱子在可解绑状态
                    putWallSlotStatus:
                        (slot.putWallSlotStatus === "BOUND" ||
                            slot.putWallSlotStatus === "DISPATCH") &&
                        slot.transferContainerCode
                            ? "OPTIONAL"
                            : "DISABLE"
                    // putWallSlotDesc: slot.putWallSlotDesc.map((val) => {
                    //     if (val.propertyName === "pickingStatus") {
                    //         return {
                    //             ...val,
                    //             propertyValue: ""
                    //         }
                    //     }
                    //     return val
                    // })
                }
            })
        }
    })
}

/**
 * 解绑适配函数
 * @param initState
 */
export const submitAdaptor = (initState: putWallViewsItem[]) => {
    let params: any = []
    initState.forEach((item) => {
        item.putWallSlots.forEach((slot: any) => {
            if (slot.putWallSlotStatus === "SELECTED") {
                params.push(slot.putWallSlotCode)
            }
        })
    })
    return params
}

/**
 * 修改当前选中状态数据适配函数
 * @param viewsData
 * @param target
 */
export const changeStateAdaptor = (
    viewsData: putWallViewsItem[],
    target: any
) => {
   
    return viewsData.map((item) => {
        if (item.active) {
            // 当前激活的页面
            return {
                ...item,
                putWallSlots: item.putWallSlots.map((slot: any) => {
                    let status = {}
                    if (slot.putWallSlotCode === target.putWallSlotCode) {
                        if (slot.putWallSlotStatus === "OPTIONAL") {
                            status = {
                                // 选中
                                putWallSlotStatus: "SELECTED"
                            }
                        } else {
                            status = {
                                // 取消
                                putWallSlotStatus: "OPTIONAL"
                            }
                        }
                    }
                    return {
                        ...slot,
                        ...status
                    }
                })
            }
        }
        return item
    })
}
