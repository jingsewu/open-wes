import startConfig from "./startTask"
import endConfig from "./existTask"
import pauseConfig from "./pauseTask"
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"

const defaultConfigs: Record<TabActionType, TabAction> = {
    [TabActionType.START_TASK]: startConfig,
    [TabActionType.EXIT]: endConfig,
    [TabActionType.STOP_TASK]: pauseConfig
}

export default defaultConfigs
