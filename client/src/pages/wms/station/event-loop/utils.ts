
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionType } from "../tab-actions/constant"

export const returnActions = (arr: (TabActionType | Partial<TabAction>)[]) => {
    const footActionsMap = new Map()
    const actions: TabAction[] = []
    const noPermissionsList: TabAction[] = []
    arr.forEach((item: any) => {
        if (item.permissions) {
            if (item.key === TabActionType.START_TASK) {
                footActionsMap.set(item.permissions.toString() + item.key, item)
                return
            }
            footActionsMap.set(item.permissions.toString(), item)
        } else {
            noPermissionsList.push(item)
        }
    })
    return actions.concat(noPermissionsList)
}

export const returnButton = (permissionsList: number[]) => {
    return true
}

export const abnormalVoiceTips = async () => {
    // const { code, data } = await Get(`/wms/sysConfig/getConfigErrorTipInfo`)
    // const { code, data }: any = await request({
    //     method: "get",
    //     url: "/wms/sysConfig/getConfigErrorTipInfo"
    // })
    // if (code !== "0") {
    //     return
    // }
    // if (data?.enabled) {
    //     const voice = new Audio(`/assets/voice/${data?.type}.wav`)
    //     voice.loop = true
    //     voice.playbackRate = 2
    //     voice.pause()
    //     voice.play().then((r) => {})
    //     setTimeout(() => {
    //         voice.pause()
    //     }, 1000 * parseInt(data?.duration, 10))
    // }
}
