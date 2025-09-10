import { observable, action, computed, runInAction, decorate } from 'mobx'
import './mobxConfig'
import type { WorkStationView } from '../event-loop/types'
import { WorkStationStatus, ChooseArea } from '../event-loop/types'

/**
 * 工作站状态管理Store
 * 使用MobX进行响应式状态管理，替代原有的useState方案
 */
export class WorkStationStore {
  // 核心状态
  workStationEvent: WorkStationView<any> | undefined = undefined
  workStationStatus: WorkStationStatus = WorkStationStatus.OFFLINE
  chooseArea: ChooseArea = ChooseArea.workLocationArea
  
  // 操作状态
  operationsMap = new Map<string, any>()
  isLoading = false
  error: string | null = null
  
  // 扫描状态
  scanCode: string | null = null
  isScanning = false
  
  // 容器状态
  callContainerCount = 0
  stationProcessingStatus: 'NO_TASK' | 'WAIT_ROBOT' | 'WAIT_CALL_CONTAINER' | undefined = undefined
  
  // 调试状态
  debugMode = false
  mockData: any = null

  // Actions
  setWorkStationEvent(event: WorkStationView<any> | undefined) {
    this.workStationEvent = event
    if (event) {
      this.workStationStatus = event.workStationStatus
      this.chooseArea = event.chooseArea
      this.scanCode = event.scanCode || null
      this.callContainerCount = event.callContainerCount || 0
      this.stationProcessingStatus = event.stationProcessingStatus
    }
  }

  setChooseArea(area: ChooseArea) {
    this.chooseArea = area
    if (this.workStationEvent) {
      this.workStationEvent.chooseArea = area
    }
  }

  setScanCode(code: string | null) {
    this.scanCode = code
    if (this.workStationEvent) {
      this.workStationEvent.scanCode = code || undefined
    }
  }

  setLoading(loading: boolean) {
    this.isLoading = loading
  }

  setError(error: string | null) {
    this.error = error
  }

  setOperationMap(type: string, operation: any) {
    this.operationsMap.set(type, operation)
  }

  removeOperation(type: string) {
    this.operationsMap.delete(type)
  }

  setDebugMode(debug: boolean, mockData?: any) {
    this.debugMode = debug
    this.mockData = mockData
  }

  // 批量更新状态
  updateWorkStationState(updates: Partial<WorkStationView<any>>) {
    runInAction(() => {
      if (this.workStationEvent) {
        Object.assign(this.workStationEvent, updates)
      }
    })
  }

  // 重置状态
  reset() {
    runInAction(() => {
      this.workStationEvent = undefined
      this.workStationStatus = WorkStationStatus.OFFLINE
      this.chooseArea = ChooseArea.workLocationArea
      this.operationsMap.clear()
      this.isLoading = false
      this.error = null
      this.scanCode = null
      this.isScanning = false
      this.callContainerCount = 0
      this.stationProcessingStatus = undefined
    })
  }

  // Computed values
  isOnline() {
    return this.workStationStatus === WorkStationStatus.ONLINE
  }

  hasActiveTask() {
    return this.workStationStatus === WorkStationStatus.DO_OPERATION
  }

  isWaiting() {
    return [
      WorkStationStatus.WAIT_ROBOT, 
      WorkStationStatus.WAIT_CONTAINER, 
      WorkStationStatus.WAIT_CALL_CONTAINER
    ].includes(this.workStationStatus)
  }

  currentOperation() {
    return this.operationsMap.get(this.chooseArea)
  }

  workStationId() {
    return this.workStationEvent?.workStationId || ''
  }

  stationCode() {
    return this.workStationEvent?.stationCode || ''
  }
}

// MobX 4.x 装饰器配置
decorate(WorkStationStore, {
  // 状态属性
  workStationEvent: observable,
  workStationStatus: observable,
  chooseArea: observable,
  operationsMap: observable,
  isLoading: observable,
  error: observable,
  scanCode: observable,
  isScanning: observable,
  callContainerCount: observable,
  stationProcessingStatus: observable,
  debugMode: observable,
  mockData: observable,
  
  // 动作方法
  setWorkStationEvent: action,
  setChooseArea: action,
  setScanCode: action,
  setLoading: action,
  setError: action,
  setOperationMap: action,
  removeOperation: action,
  setDebugMode: action,
  updateWorkStationState: action,
  reset: action,
  
  // 计算属性 - 这些是方法而不是 getter，所以不需要 computed 装饰器
})

// 创建单例实例
export const workStationStore = new WorkStationStore()
