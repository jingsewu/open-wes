
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
