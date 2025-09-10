// 状态管理入口文件
export { workStationStore } from "./WorkStationStore"
import { workStationStore } from "./WorkStationStore"

// 废弃的 hooks，建议使用 useWorkStation 替代
export const useWorkStationStore = () => workStationStore
export const useWorkStationAPI = () => {
    const WorkStationEventLoop = require("../event-loop/index").default
    const Message = require("../widgets/message").default
    const workStationEventLoop = new WorkStationEventLoop()
    
    return { 
        message: Message, 
        onActionDispatch: workStationEventLoop.actionDispatch 
    }
}

// 导出所有Hooks
export {
    useWorkStation,
    useWorkStationState,
    useWorkStationComputed,
    useWorkStationActions,
    useWorkStationArea,
    useWorkStationScan,
    useWorkStationContainer,
    observer
} from "./hooks/useWorkStation"

// 导出扫码枪 Hook
export { useBarcodeScanner } from "./hooks/useBarcodeScanner"

// 导出类型
export type {
    WorkStationView,
    WorkStationStatus,
    ChooseArea
} from "../event-loop/types"
