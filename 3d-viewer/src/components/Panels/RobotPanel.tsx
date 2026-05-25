import { useState } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { RobotStatusType } from '@/types'

const STATUS_DOT: Record<RobotStatusType, string> = {
  IDLE: 'bg-blue-400',
  MOVING_TO_PICKUP: 'bg-green-400',
  LOADING: 'bg-yellow-400',
  MOVING_TO_DESTINATION: 'bg-green-400',
  UNLOADING: 'bg-yellow-400',
  CHARGING: 'bg-gray-400',
  ERROR: 'bg-red-500',
}

export function RobotPanel() {
  const [collapsed, setCollapsed] = useState(false)
  const robots = useSimulatorStore(s => s.robots)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const selectRobot = useSimulatorStore(s => s.selectRobot)
  const setCameraMode = useSimulatorStore(s => s.setCameraMode)

  const handleFollowRobot = (robotCode: string) => {
    selectRobot(robotCode)
    setCameraMode('follow')
  }

  if (collapsed) {
    return (
      <button
        onClick={() => setCollapsed(false)}
        className="absolute left-0 top-12 bg-white/90 backdrop-blur px-2 py-1 border border-gray-200 rounded-r text-xs"
      >
        Robots ({robots.length})
      </button>
    )
  }

  return (
    <div className="absolute left-0 top-12 bottom-0 w-56 bg-white/90 backdrop-blur border-r border-gray-200 overflow-y-auto p-3">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-semibold text-gray-700">Robots ({robots.length})</h3>
        <button onClick={() => setCollapsed(true)} className="text-gray-400 hover:text-gray-600 text-xs">Hide</button>
      </div>
      {robots.map(robot => (
        <div
          key={robot.robotCode}
          className={`mb-1.5 p-2 rounded text-xs cursor-pointer transition-colors ${
            robot.robotCode === selectedRobotCode ? 'bg-blue-50 border border-blue-200' : 'bg-gray-50 hover:bg-gray-100'
          }`}
          onClick={() => selectRobot(robot.robotCode === selectedRobotCode ? null : robot.robotCode)}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <span className={`w-2 h-2 rounded-full ${STATUS_DOT[robot.status]}`} />
              <span className="font-mono font-medium">{robot.robotCode}</span>
            </div>
            <button
              onClick={(e) => { e.stopPropagation(); handleFollowRobot(robot.robotCode) }}
              className="text-blue-500 hover:text-blue-700 text-[10px]"
            >
              Follow
            </button>
          </div>
          <div className="mt-1 text-gray-400">
            {robot.status} {robot.carriedContainerCode ? `| ${robot.carriedContainerCode}` : ''}
          </div>
          {/* Battery bar */}
          <div className="mt-1 h-1 bg-gray-200 rounded-full">
            <div
              className={`h-full rounded-full ${robot.batteryLevel > 0.3 ? 'bg-green-400' : 'bg-red-400'}`}
              style={{ width: `${robot.batteryLevel * 100}%` }}
            />
          </div>
        </div>
      ))}
    </div>
  )
}
