import WorkStationEventLoop from "./index"

/**
 * 全局唯一的 WorkStationEventLoop 实例
 * 由 station/index.tsx 负责 start/pause/initListener
 * 由 useStationSession 的 clearStation() 负责 destroy()
 * 由 useWorkStation hook 负责 actionDispatch
 */
export const workStationEventLoop = new WorkStationEventLoop()
