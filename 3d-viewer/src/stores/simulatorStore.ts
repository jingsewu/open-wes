import { create } from 'zustand'
import type { RobotState, TaskState, WarehouseLayout, ConnectionStatus, CameraMode } from '@/types'

interface SimulatorState {
  // Data
  robots: RobotState[]
  previousRobots: RobotState[]
  tasks: TaskState[]
  completedTasks: TaskState[]
  layout: WarehouseLayout | null
  lastUpdateTime: number

  // UI state
  connectionStatus: ConnectionStatus
  selectedRobotCode: string | null
  cameraMode: CameraMode
  simulationSpeed: number

  // Actions
  updateRobotState: (robots: RobotState[], tasks: TaskState[]) => void
  setLayout: (layout: WarehouseLayout) => void
  setConnectionStatus: (status: ConnectionStatus) => void
  selectRobot: (robotCode: string | null) => void
  setCameraMode: (mode: CameraMode) => void
  setSimulationSpeed: (speed: number) => void
}

const MAX_COMPLETED_TASKS = 20

export const useSimulatorStore = create<SimulatorState>((set, get) => ({
  robots: [],
  previousRobots: [],
  tasks: [],
  completedTasks: [],
  layout: null,
  lastUpdateTime: 0,

  connectionStatus: 'disconnected',
  selectedRobotCode: null,
  cameraMode: 'perspective',
  simulationSpeed: 1.0,

  updateRobotState: (robots, tasks) => {
    const state = get()
    // Track completed tasks
    const activeCodes = new Set(tasks.map(t => t.taskCode))
    const newlyCompleted = state.tasks.filter(t => !activeCodes.has(t.taskCode))

    set({
      previousRobots: state.robots,
      robots,
      tasks,
      completedTasks: [...newlyCompleted, ...state.completedTasks].slice(0, MAX_COMPLETED_TASKS),
      lastUpdateTime: Date.now(),
    })
  },

  setLayout: (layout) => set({ layout }),
  setConnectionStatus: (status) => set({ connectionStatus: status }),
  selectRobot: (robotCode) => set({ selectedRobotCode: robotCode }),
  setCameraMode: (mode) => set({ cameraMode: mode }),
  setSimulationSpeed: (speed) => set({ simulationSpeed: speed }),
}))
