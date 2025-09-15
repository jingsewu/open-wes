import { useMemo, useCallback } from 'react'
import { observer } from 'mobx-react-lite'
import { workStationStore } from '../WorkStationStore'
import type { WorkStationView, ChooseArea } from '../../event-loop/types'
import Message from '../../widgets/message'
import WorkStationEventLoop from '../../event-loop/index'

/**
 * 优化的工作站Hook
 * 提供性能优化的状态访问和操作方法
 */

// 事件循环实例
const workStationEventLoop = new WorkStationEventLoop()

/**
 * 使用工作站状态的Hook
 * 不依赖 Context，直接使用全局实例
 */
export const useWorkStation = () => {
    // 直接使用全局实例，不依赖 Context
    const store = workStationStore
    const message = Message
    const onActionDispatch = workStationEventLoop.actionDispatch
    
    // 从 store 中获取所有状态和操作方法
    const state = {
        workStationEvent: store.workStationEvent,
        workStationStatus: store.workStationStatus,
        // 注意：chooseArea和scanCode都是从getApiData接口返回的值，不能主动设置
        chooseArea: store.chooseArea,
        scanCode: store.scanCode
    }
    
    const computed = {
        workStationId: store.workStationId,
        stationCode: store.stationCode,
        stationProcessingStatus: store.stationProcessingStatus
    }
    
    const actions = {
        setOperationMap: (type: string, operation: any) => store.setOperationMap(type, operation),
        updateWorkStationState: (updates: Partial<WorkStationView<any>>) => store.updateWorkStationState(updates),
        reset: () => store.reset()
    }
    
    // 返回合并后的结果
    return {
        // 全局实例
        store,
        message,
        onActionDispatch,
        
        // 性能优化的状态和操作
        ...state,
        ...computed,
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
