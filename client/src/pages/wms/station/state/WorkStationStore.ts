import { observable, action, computed, runInAction, reaction } from 'mobx'
import './mobxConfig'
import type { WorkStationView } from '../event-loop/types'
import { WorkStationStatus, ChooseArea } from '../event-loop/types'

/**
 * 工作站状态管理Store
 * 使用MobX进行响应式状态管理，替代原有的useState方案
 */
export class WorkStationStore {
  // 核心状态
  @observable workStationEvent: WorkStationView<any> | undefined = undefined
  @observable workStationStatus: WorkStationStatus = WorkStationStatus.OFFLINE
  
  // 操作状态
  @observable operationsMap = new Map<string, any>()
  
  // 扫描状态
  @observable scanCode: string | null = null
  
  // 容器状态
  @observable callContainerCount = 0
  @observable stationProcessingStatus: 'NO_TASK' | 'WAIT_ROBOT' | 'WAIT_CALL_CONTAINER' | undefined = undefined



  @action setWorkStationEvent(event: WorkStationView<any> | undefined) {
    if (this.workStationEvent === event) {
      return
    }
    
    runInAction(() => {
      this.workStationEvent = event
      
      if (event) {
        this.batchUpdateStates(event)
      }
    })
  }

  @action setOperationMap(type: string, operation: any) {
    if (this.operationsMap.get(type) === operation) {
      return
    }
    this.operationsMap.set(type, operation)
  }

  @action updateWorkStationState(updates: Partial<WorkStationView<any>>) {
    runInAction(() => {
      if (this.workStationEvent) {
        Object.assign(this.workStationEvent, updates)
        
        if (updates.workStationStatus !== undefined) {
          this.workStationStatus = updates.workStationStatus
        }
        if (updates.scanCode !== undefined) {
          this.scanCode = updates.scanCode || null
        }
        if (updates.callContainerCount !== undefined) {
          this.callContainerCount = updates.callContainerCount || 0
        }
        if (updates.stationProcessingStatus !== undefined) {
          this.stationProcessingStatus = updates.stationProcessingStatus
        }
      }
    })
  }

  @action reset() {
    runInAction(() => {
      this.workStationEvent = undefined
      this.workStationStatus = WorkStationStatus.OFFLINE
      this.operationsMap.clear()
      this.scanCode = null
      this.callContainerCount = 0
      this.stationProcessingStatus = undefined
    })
  }

  @computed get chooseArea() {
    return this.workStationEvent?.chooseArea || ChooseArea.workLocationArea
  }

  @computed get workStationId() {
    return this.workStationEvent?.workStationId || ''
  }

  @computed get stationCode() {
    return this.workStationEvent?.stationCode || ''
  }


  private batchUpdateStates(event: WorkStationView<any>): void {
    this.workStationStatus = event.workStationStatus
    this.scanCode = event.scanCode || null
    this.callContainerCount = event.callContainerCount || 0
    this.stationProcessingStatus = event.stationProcessingStatus
  }

  dispose(): void {
    this.operationsMap.clear()
  }
}

// 创建单例实例
export const workStationStore = new WorkStationStore()

// 使用 MobX 4.x 的简单配置方式
// 直接在属性上使用 observable 和 action
