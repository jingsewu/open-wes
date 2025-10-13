import { useMemo, useCallback } from 'react'
import { observer } from 'mobx-react-lite'
import { workStationStore } from '../WorkStationStore'
import type { WorkStationView, ChooseArea } from '../../event-loop/types'
import Message from '../../widgets/message'
import WorkStationEventLoop from '../../event-loop/index'

/**
 * 工作站Hook
 * 提供状态访问和操作方法
 */

// 事件循环实例
const workStationEventLoop = new WorkStationEventLoop()

/**
 * 使用工作站状态的Hook
 * 不依赖 Context，直接使用全局实例
 */
export const useWorkStation = () => {
    const store = workStationStore
    
    const actions = useMemo(() => ({
        setOperationMap: (type: string, operation: any) => store.setOperationMap(type, operation),
        updateWorkStationState: (updates: Partial<WorkStationView<any>>) => store.updateWorkStationState(updates),
        reset: () => store.reset()
    }), [store])
    
    return {
        store,
        message: Message,
        onActionDispatch: workStationEventLoop.actionDispatch,
        workStationEvent: store.workStationEvent,
        workStationStatus: store.workStationStatus,
        chooseArea: store.chooseArea,
        scanCode: store.scanCode,
        workStationId: store.workStationId,
        stationCode: store.stationCode,
        stationProcessingStatus: store.stationProcessingStatus,
        ...actions
    }
}


// 特定区域状态Hook
export const useWorkStationArea = (areaType: string) => {
  // 直接使用 store 来确保响应式更新
  const store = workStationStore
  const chooseArea = store.chooseArea
  const isActive = chooseArea === areaType

  const operation = useMemo(() => {
    return store.operationsMap.get(areaType)
  }, [store.operationsMap, areaType])

  console.log("useWorkStationArea:", { areaType, chooseArea, isActive })

  return {
    isActive,
    operation
  }
}



// 导出observer包装的组件
export { observer }
