import _ from "lodash"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import { submitAdaptor } from "@/pages/wms/station/instances/outbound/custom-actions/UnbindBox/utils"
import { MessageType } from "@/pages/wms/station/widgets/message"
import type { putWallViewsItem } from "@/pages/wms/station/event-loop/types"

/**
 * 请求保存当前的数据到服务端
 * @param viewsData
 * @param context
 */
export const onSaveRequest = async (
    viewsData: putWallViewsItem[],
    context: any
) => {
    let putWallSlotCodes = submitAdaptor(viewsData)
    // 如果没有选中任何内容，直接关闭当前的虚拟框
    if (_.isEmpty(putWallSlotCodes)) {
        return true
    }
    const { code, msg } = await context.onCustomActionDispatch({
        eventCode: CustomActionType.UNBIND,
        data: {
            putWallSlotCodes
        }
    })

    // if (code !== "0") {
    //     context.message?.({
    //         type: MessageType.ERROR,
    //         content: msg
    //     })
    //     return false
    // }

    return code !== "-1"
}
