// 状态管理入口文件
export { workStationStore } from "./WorkStationStore"

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
