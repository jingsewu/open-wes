import React, { useMemo, useCallback, useRef } from 'react'
import { observer } from 'mobx-react-lite'
import { workStationStore } from '../WorkStationStore'
import type { WorkStationView } from '../../event-loop/types'
import Message from '../../widgets/message'
import WorkStationEventLoop from '../../event-loop/index'

const globalEventLoopInstance = new WorkStationEventLoop()

export const useWorkStation = () => {
    const store = workStationStore
    const eventLoopRef = useRef(globalEventLoopInstance)
    
    const actions = {
        setOperationMap: store.setOperationMap.bind(store),
        updateWorkStationState: store.updateWorkStationState.bind(store),
        reset: store.reset.bind(store)
    }
    
    const actionDispatch = useCallback((payload: any) => {
        return eventLoopRef.current.actionDispatch(payload)
    }, [])
    
    return useMemo(() => ({
        store,
        message: Message,
        onActionDispatch: actionDispatch,
        workStationEvent: store.workStationEvent,
        workStationStatus: store.workStationStatus,
        chooseArea: store.chooseArea,
        scanCode: store.scanCode,
        workStationId: store.workStationId,
        stationCode: store.stationCode,
        stationProcessingStatus: store.stationProcessingStatus,
        ...actions
    }), [
        actionDispatch,
        store.workStationEvent,
        store.workStationStatus,
        store.chooseArea,
        store.scanCode,
        store.workStationId,
        store.stationCode,
        store.stationProcessingStatus
    ])
}


export const useWorkStationArea = (areaType: string) => {
  const store = workStationStore
  const chooseArea = store.chooseArea
  const isActive = chooseArea === areaType

  // 直接获取operation，Map.get()已经足够快，不需要useMemo
  const operation = store.operationsMap.get(areaType)

  return {
    isActive,
    operation
  }
}

export { observer }
