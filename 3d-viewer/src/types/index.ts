export interface RobotState {
  robotCode: string
  robotType: string
  status: RobotStatusType
  x: number
  y: number
  rotation: number
  carriedContainerCode: string | null
  taskCode: string | null
  batteryLevel: number
}

export type RobotStatusType =
  | 'IDLE'
  | 'MOVING_TO_PICKUP'
  | 'LOADING'
  | 'MOVING_TO_DESTINATION'
  | 'UNLOADING'
  | 'CHARGING'
  | 'ERROR'

export interface TaskState {
  taskCode: string
  status: string
  containerCode: string
  startLocation: string
  destination: string | null
  assignedRobot: string | null
}

export interface WebSocketMessage {
  type: 'ROBOT_STATE_UPDATE'
  timestamp: number
  robots: RobotState[]
  tasks: TaskState[]
}

export interface WarehouseConfig {
  width: number
  height: number
  gridSize: number
}

export interface ShelfConfig {
  id: string
  x: number
  y: number
  width: number
  height: number
  locationCodes: string[]
}

export interface WorkstationConfig {
  id: string
  x: number
  y: number
  type: string
  locationCode: string
}

export interface ChargingStationConfig {
  id: string
  x: number
  y: number
  locationCode: string
}

export interface RobotConfig {
  robotCode: string
  robotType: string
  startX: number
  startY: number
  speed: number
}

export interface WarehouseLayout {
  warehouse: WarehouseConfig
  shelves: ShelfConfig[]
  workstations: WorkstationConfig[]
  chargingStations: ChargingStationConfig[]
  robots: RobotConfig[]
}

export type ConnectionStatus = 'connected' | 'connecting' | 'disconnected'

export type CameraMode = 'perspective' | 'topdown' | 'follow'
