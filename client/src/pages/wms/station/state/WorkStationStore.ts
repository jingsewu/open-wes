import { observable, action, computed, runInAction } from 'mobx'
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

  // Actions
  @action setWorkStationEvent(event: WorkStationView<any> | undefined) {
    console.log("setWorkStationEvent called with:", event)
    
    runInAction(() => {
      if (this.workStationEvent === event) {
        console.log("WorkStationEvent unchanged (same reference), skipping update")
        return
      }
      
      // 保存之前的状态用于比较
      const previousChooseArea = this.workStationEvent?.chooseArea
      
      // 更新事件数据
      this.workStationEvent = event
      
      if (event) {
        // 更新所有相关状态
        this.workStationStatus = event.workStationStatus
        this.scanCode = event.scanCode || null
        this.callContainerCount = event.callContainerCount || 0
        this.stationProcessingStatus = event.stationProcessingStatus
        
        // 检查 chooseArea 是否发生变化
        const currentChooseArea = event.chooseArea
        if (previousChooseArea !== currentChooseArea) {
          console.log("chooseArea 发生变化:", {
            from: previousChooseArea,
            to: currentChooseArea
          })
        }
        
        console.log("WorkStationEvent updated successfully:", {
          chooseArea: currentChooseArea,
          workStationStatus: this.workStationStatus,
          scanCode: this.scanCode
        })
      } else {
        console.log("WorkStationEvent cleared")
      }
    })
  }

  @action setOperationMap(type: string, operation: any) {
    this.operationsMap.set(type, operation)
  }

  // 批量更新状态
  @action updateWorkStationState(updates: Partial<WorkStationView<any>>) {
    runInAction(() => {
      if (this.workStationEvent) {
        Object.assign(this.workStationEvent, updates)
      }
    })
  }

  // 重置状态
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

  // Computed values
  @computed get chooseArea() {
    const area = this.workStationEvent?.chooseArea || ChooseArea.workLocationArea
    console.log("WorkStationStore chooseArea computed:", {
      area,
      workStationEvent: this.workStationEvent ? 'exists' : 'null',
      eventChooseArea: this.workStationEvent?.chooseArea,
      timestamp: new Date().toISOString()
    })
    return area
  }

  @computed get workStationId() {
    return this.workStationEvent?.workStationId || ''
  }

  @computed get stationCode() {
    return this.workStationEvent?.stationCode || ''
  }
}

// 创建单例实例
export const workStationStore = new WorkStationStore()

// 使用 MobX 4.x 的简单配置方式
// 直接在属性上使用 observable 和 action
