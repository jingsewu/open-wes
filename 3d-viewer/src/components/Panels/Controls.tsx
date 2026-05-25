import { useSimulatorStore } from '@/stores/simulatorStore'
import { useSimulatorApi } from '@/hooks/useSimulatorApi'
import { ConnectionStatus } from './ConnectionStatus'
import type { CameraMode } from '@/types'

const CAMERA_LABELS: Record<CameraMode, string> = {
  perspective: 'Perspective',
  topdown: 'Top-Down',
  follow: 'Follow',
}

export function Controls() {
  const cameraMode = useSimulatorStore(s => s.cameraMode)
  const setCameraMode = useSimulatorStore(s => s.setCameraMode)
  const simulationSpeed = useSimulatorStore(s => s.simulationSpeed)
  const setSimulationSpeed = useSimulatorStore(s => s.setSimulationSpeed)
  const { resetSimulator, updateConfig } = useSimulatorApi()

  const handleSpeedChange = async (speed: number) => {
    setSimulationSpeed(speed)
    await updateConfig({ defaultRobotSpeed: 2.0 * speed })
  }

  const cameraModes: CameraMode[] = ['perspective', 'topdown', 'follow']

  return (
    <div className="absolute top-0 left-0 right-0 h-12 bg-white/90 backdrop-blur border-b border-gray-200 flex items-center px-4 gap-4 z-10">
      <h1 className="text-sm font-bold text-gray-800 mr-4">Open WES 3D Viewer</h1>

      <ConnectionStatus />

      {/* Camera mode */}
      <div className="flex items-center gap-1 ml-4">
        <span className="text-xs text-gray-500">Camera:</span>
        {cameraModes.map(mode => (
          <button
            key={mode}
            onClick={() => setCameraMode(mode)}
            className={`px-2 py-0.5 rounded text-xs ${
              cameraMode === mode ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {CAMERA_LABELS[mode]}
          </button>
        ))}
      </div>

      {/* Speed slider */}
      <div className="flex items-center gap-2 ml-4">
        <span className="text-xs text-gray-500">Speed:</span>
        <input
          type="range"
          min="0.5"
          max="5"
          step="0.5"
          value={simulationSpeed}
          onChange={e => handleSpeedChange(parseFloat(e.target.value))}
          className="w-20 h-1"
        />
        <span className="text-xs font-mono w-8">{simulationSpeed}x</span>
      </div>

      {/* Reset */}
      <button
        onClick={resetSimulator}
        className="ml-auto px-3 py-1 rounded text-xs bg-red-50 text-red-600 hover:bg-red-100"
      >
        Reset
      </button>
    </div>
  )
}
