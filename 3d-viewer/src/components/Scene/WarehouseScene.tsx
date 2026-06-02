import { Canvas } from '@react-three/fiber'
import { Text } from '@react-three/drei'
import { useSimulatorStore } from '@/stores/simulatorStore'
import { Floor } from './Floor'
import { Shelf } from './Shelf'
import { Robot } from './Robot'
import { Workstation } from './Workstation'
import { CameraController } from './CameraController'

function ChargingStation({ config }: { config: { id: string; x: number; y: number } }) {
  return (
    <group position={[config.x, 0, config.y]}>
      {/* Base pad */}
      <mesh position={[0, 0.02, 0]} receiveShadow>
        <boxGeometry args={[0.6, 0.04, 0.6]} />
        <meshStandardMaterial color="#8ab8b8" metalness={0.3} roughness={0.6} />
      </mesh>
      {/* Charger post */}
      <mesh position={[0, 0.2, 0]}>
        <boxGeometry args={[0.08, 0.4, 0.08]} />
        <meshStandardMaterial color="#6a9a9a" metalness={0.5} roughness={0.4} />
      </mesh>
      {/* Charging indicator */}
      <mesh position={[0, 0.45, 0]}>
        <sphereGeometry args={[0.035, 8, 8]} />
        <meshBasicMaterial color="#00ddcc" transparent opacity={0.7} />
      </mesh>
      {/* Label */}
      <Text position={[0, 0.6, 0]} fontSize={0.1} color="#3a6a6a" anchorX="center" anchorY="bottom">
        {config.id}
      </Text>
    </group>
  )
}

function Pod({ config }: { config: { id: string; x: number; y: number } }) {
  return (
    <group position={[config.x, 0, config.y]}>
      {/* Pod base */}
      <mesh position={[0, 0.08, 0]} receiveShadow>
        <boxGeometry args={[0.5, 0.06, 0.5]} />
        <meshStandardMaterial color="#7a9a5a" metalness={0.2} roughness={0.7} />
      </mesh>
      {/* Pod legs (4 corners) */}
      {[
        [-0.18, -0.18],
        [0.18, -0.18],
        [-0.18, 0.18],
        [0.18, 0.18],
      ].map(([px, pz], i) => (
        <mesh key={i} position={[px, 0.03, pz]}>
          <cylinderGeometry args={[0.015, 0.02, 0.04, 6]} />
          <meshStandardMaterial color="#5a7a3a" metalness={0.3} roughness={0.6} />
        </mesh>
      ))}
      {/* Pod top plate */}
      <mesh position={[0, 0.18, 0]}>
        <boxGeometry args={[0.45, 0.04, 0.45]} />
        <meshStandardMaterial color="#8aaa6a" metalness={0.3} roughness={0.6} />
      </mesh>
      {/* Label */}
      <Text position={[0, 0.28, 0]} fontSize={0.08} color="#4a6a3a" anchorX="center" anchorY="bottom">
        {config.id}
      </Text>
    </group>
  )
}

export function WarehouseScene() {
  const layout = useSimulatorStore(s => s.layout)
  const robots = useSimulatorStore(s => s.robots)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const selectRobot = useSimulatorStore(s => s.selectRobot)

  return (
    <Canvas
      shadows
      gl={{ antialias: true }}
      camera={{ fov: 45, near: 0.1, far: 500 }}
      style={{ background: '#d8dce0' }}
    >
      {/* ─── Bright ambient lighting ─── */}
      <hemisphereLight
        args={['#8ab4d6', '#d8dce0', 0.8]}
        position={[0, 50, 0]}
      />
      <ambientLight intensity={0.5} color="#d0d8e0" />

      {/* Main directional */}
      <directionalLight
        position={[40, 60, 30]}
        intensity={1.0}
        castShadow
        shadow-mapSize-width={2048}
        shadow-mapSize-height={2048}
        shadow-camera-left={-60}
        shadow-camera-right={60}
        shadow-camera-top={60}
        shadow-camera-bottom={-60}
        shadow-bias={-0.001}
      />

      {/* Fill light */}
      <directionalLight position={[-30, 20, -20]} intensity={0.3} color="#b0c8d8" />

      {/* Overhead fill */}
      <directionalLight position={[0, 80, 0]} intensity={0.15} color="#c0d0e0" />

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

          {layout.chargingStations?.map(cs => (
            <ChargingStation key={cs.id} config={cs} />
          ))}

          {layout.pods?.map(pod => (
            <Pod key={pod.id} config={pod} />
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
