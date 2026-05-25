import { Canvas } from '@react-three/fiber'
import { useSimulatorStore } from '@/stores/simulatorStore'
import { Floor } from './Floor'
import { Shelf } from './Shelf'
import { Robot } from './Robot'
import { Workstation } from './Workstation'
import { CameraController } from './CameraController'

export function WarehouseScene() {
  const layout = useSimulatorStore(s => s.layout)
  const robots = useSimulatorStore(s => s.robots)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const selectRobot = useSimulatorStore(s => s.selectRobot)

  return (
    <Canvas shadows camera={{ fov: 50, near: 0.1, far: 500 }}>
      <ambientLight intensity={0.6} />
      <directionalLight position={[30, 40, 20]} intensity={0.8} castShadow />

      <CameraController />

      {layout && (
        <>
          <Floor width={layout.warehouse.width} height={layout.warehouse.height} />
          {layout.shelves.map(shelf => (
            <Shelf key={shelf.id} config={shelf} />
          ))}
          {layout.workstations.map(ws => (
            <Workstation key={ws.id} config={ws} />
          ))}
        </>
      )}

      {robots.map(robot => (
        <Robot
          key={robot.robotCode}
          robot={robot}
          isSelected={robot.robotCode === selectedRobotCode}
          onClick={() => selectRobot(robot.robotCode === selectedRobotCode ? null : robot.robotCode)}
        />
      ))}
    </Canvas>
  )
}
