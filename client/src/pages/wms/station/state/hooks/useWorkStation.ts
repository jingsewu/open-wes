import { useMemo, useCallback, useRef, useEffect } from 'react'
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
        chooseArea: store.chooseArea,
        scanCode: store.scanCode,
        isOnline: store.isOnline(),
        hasActiveTask: store.hasActiveTask()
    }
    
    const computed = {
        workStationId: store.workStationId(),
        stationCode: store.stationCode(),
        currentOperation: store.currentOperation(),
        operationCount: store.operationsMap.size,
        callContainerCount: store.callContainerCount,
        stationProcessingStatus: store.stationProcessingStatus
    }
    
    const actions = {
        setChooseArea: (area: ChooseArea) => store.setChooseArea(area),
        setScanCode: (code: string | null) => store.setScanCode(code),
        setOperationMap: (type: string, operation: any) => store.setOperationMap(type, operation),
        removeOperation: (type: string) => store.removeOperation(type),
        updateWorkStationState: (updates: Partial<WorkStationView<any>>) => store.updateWorkStationState(updates),
        reset: () => store.reset(),
        // 扫描相关操作
        clearScanCode: () => store.setScanCode(null),
        // 容器相关操作
        updateProcessingStatus: (status: 'NO_TASK' | 'WAIT_ROBOT' | 'WAIT_CALL_CONTAINER') => 
            store.updateWorkStationState({ stationProcessingStatus: status })
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

// 基础状态Hook
export const useWorkStationState = () => {
  return {
    workStationEvent: workStationStore.workStationEvent,
    workStationStatus: workStationStore.workStationStatus,
    chooseArea: workStationStore.chooseArea,
    scanCode: workStationStore.scanCode,
    isOnline: workStationStore.isOnline(),
    hasActiveTask: workStationStore.hasActiveTask()
  }
}

// 计算属性Hook
export const useWorkStationComputed = () => {
  return useMemo(() => ({
    workStationId: workStationStore.workStationId(),
    stationCode: workStationStore.stationCode(),
    currentOperation: workStationStore.currentOperation(),
    operationCount: workStationStore.operationsMap.size,
    callContainerCount: workStationStore.callContainerCount,
    stationProcessingStatus: workStationStore.stationProcessingStatus
  }), [
    workStationStore.workStationId(),
    workStationStore.stationCode(),
    workStationStore.currentOperation(),
    workStationStore.operationsMap.size,
    workStationStore.callContainerCount,
    workStationStore.stationProcessingStatus
  ])
}

// 操作方法Hook
export const useWorkStationActions = () => {
  const setChooseArea = useCallback((area: ChooseArea) => {
    workStationStore.setChooseArea(area)
  }, [])

  const setScanCode = useCallback((code: string | null) => {
    workStationStore.setScanCode(code)
  }, [])

  const setOperationMap = useCallback((type: string, operation: any) => {
    workStationStore.setOperationMap(type, operation)
  }, [])

  const removeOperation = useCallback((type: string) => {
    workStationStore.removeOperation(type)
  }, [])

  const updateWorkStationState = useCallback((updates: Partial<WorkStationView<any>>) => {
    workStationStore.updateWorkStationState(updates)
  }, [])

  const reset = useCallback(() => {
    workStationStore.reset()
  }, [])

  return {
    setChooseArea,
    setScanCode,
    setOperationMap,
    removeOperation,
    updateWorkStationState,
    reset
  }
}

// 特定区域状态Hook
export const useWorkStationArea = (areaType: string) => {
  const isActive = useMemo(() => {
    return workStationStore.chooseArea === areaType
  }, [workStationStore.chooseArea, areaType])

  const operation = useMemo(() => {
    return workStationStore.operationsMap.get(areaType)
  }, [workStationStore.operationsMap, areaType])

  const setActive = useCallback(() => {
    workStationStore.setChooseArea(areaType as ChooseArea)
  }, [areaType])

  return {
    isActive,
    operation,
    setActive
  }
}

// 扫描相关Hook
export const useWorkStationScan = () => {
  const scanCode = workStationStore.scanCode

  const setScanCode = useCallback((code: string | null) => {
    workStationStore.setScanCode(code)
  }, [])

  const clearScanCode = useCallback(() => {
    workStationStore.setScanCode(null)
  }, [])

  return {
    scanCode,
    setScanCode,
    clearScanCode
  }
}

// 容器相关Hook
export const useWorkStationContainer = () => {
  const callContainerCount = workStationStore.callContainerCount
  const stationProcessingStatus = workStationStore.stationProcessingStatus

  const updateProcessingStatus = useCallback((status: 'NO_TASK' | 'WAIT_ROBOT' | 'WAIT_CALL_CONTAINER') => {
    workStationStore.updateWorkStationState({ stationProcessingStatus: status })
  }, [])

  return {
    callContainerCount,
    stationProcessingStatus,
    updateProcessingStatus
  }
}


// 导出observer包装的组件
export { observer }
