import { useEffect } from 'react'
import { WarehouseScene } from '@/components/Scene/WarehouseScene'
import { Controls } from '@/components/Panels/Controls'
import { TaskPanel } from '@/components/Panels/TaskPanel'
import { RobotPanel } from '@/components/Panels/RobotPanel'
import { useRobotWebSocket } from '@/hooks/useRobotWebSocket'
import { useSimulatorApi } from '@/hooks/useSimulatorApi'

const WS_URL = import.meta.env.VITE_SIMULATOR_WS_URL || 'ws://localhost:8091/ws/robots'

export default function App() {
  useRobotWebSocket(WS_URL)
  const { fetchLayout } = useSimulatorApi()

  useEffect(() => {
    fetchLayout()
  }, [fetchLayout])

  return (
    <div className="w-screen h-screen relative">
      <WarehouseScene />
      <Controls />
      <RobotPanel />
      <TaskPanel />
    </div>
  )
}
