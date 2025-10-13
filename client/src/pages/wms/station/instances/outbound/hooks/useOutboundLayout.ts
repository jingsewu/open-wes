import { useCallback } from "react"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"
import { CustomActionType } from "../customActionType"
import { MessageType } from "@/pages/wms/station/widgets/message"
import { useWorkStation } from "../../../state"

/**
 * 出库布局相关的业务逻辑 Hook
 */
export const useOutboundLayout = () => {
    const { store, onActionDispatch, message } = useWorkStation()
    
    const chooseArea = store?.chooseArea

    // 区域激活状态
    const activeStates = {
        containerAreaIsActive: chooseArea === ChooseArea.workLocationArea,
        skuAreaIsActive: chooseArea === ChooseArea.skuArea,
        putWallAreaIsActive: chooseArea === ChooseArea.putWallArea,
    }

    // 区域切换处理
    const changeAreaHandler = useCallback(
        async (type: string) => {
            if (!onActionDispatch) return
            
            try {
                const { code, msg } = await onActionDispatch({
                    eventCode: CustomActionType.CHOOSE_AREA,
                    data: type
                })
                
                if (code === "-1") {
                    message?.({
                        type: MessageType.ERROR,
                        content: msg
                    })
                }
            } catch (error) {
                message?.({
                    type: MessageType.ERROR,
                    content: error.message
                })
            }
        },
        [onActionDispatch, message]
    )

    return {
        ...activeStates,
        changeAreaHandler
    }
}
