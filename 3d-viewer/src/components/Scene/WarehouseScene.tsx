import { Canvas } from '@react-three/fiber'
import { Text } from '@react-three/drei'
import { useSimulatorStore } from '@/stores/simulatorStore'
import { Floor } from './Floor'
import { Shelf } from './Shelf'
import { Robot } from './Robot'
import { Workstation } from './Workstation'
import { CameraController } from './CameraController'
import type { ShelfConfig } from '@/types'

function shelvesWithContainers(shelves: ShelfConfig[], stored: string[] | undefined) {
  const storedSet = new Set(stored ?? [])
  return shelves.map(shelf => (
    <Shelf key={shelf.id} config={shelf} storedLocations={storedSet} />
  ))
}

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

// ═══ Warehouse Pod (料架) — lightweight shelving unit for KIVA AGV ═══
function Pod({ config }: { config: { id: string; x: number; y: number } }) {
  const PW = 0.65
  const PD = 0.65
  const LEG_H = 0.12 // ground clearance (AGV slides under)
  const hw = PW / 2
  const hd = PD / 2

  const BIN_COLORS = ['#b8b8b8', '#a8b8b0', '#b0b0b8']

  return (
    <group position={[config.x, 0, config.y]}>
      {/* ═══ 4 corner legs ═══ */}
      {[
        [-hw + 0.06, -hd + 0.06],
        [hw - 0.06, -hd + 0.06],
        [-hw + 0.06, hd - 0.06],
        [hw - 0.06, hd - 0.06],
      ].map(([px, pz], i) => (
        <mesh key={`leg-${i}`} position={[px, LEG_H / 2, pz]} castShadow>
          <cylinderGeometry args={[0.025, 0.03, LEG_H, 6]} />
          <meshStandardMaterial color="#6a7a8a" metalness={0.6} roughness={0.5} />
        </mesh>
      ))}

      {/* ═══ Base frame (bottom plate) ═══ */}
      <mesh position={[0, LEG_H + 0.01, 0]} receiveShadow>
        <boxGeometry args={[PW - 0.04, 0.025, PD - 0.04]} />
        <meshStandardMaterial color="#7a8a9a" metalness={0.5} roughness={0.6} />
      </mesh>

      {/* ═══ 4 corner upright posts ═══ */}
      {[
        [-hw + 0.06, -hd + 0.06],
        [hw - 0.06, -hd + 0.06],
        [-hw + 0.06, hd - 0.06],
        [hw - 0.06, hd - 0.06],
      ].map(([px, pz], i) => (
        <mesh key={`post-${i}`} position={[px, LEG_H + 0.20, pz]} castShadow>
          <cylinderGeometry args={[0.015, 0.018, 0.35, 6]} />
          <meshStandardMaterial color="#8a9aaa" metalness={0.5} roughness={0.5} />
        </mesh>
      ))}

      {/* ═══ Shelf level 1 (middle) ═══ */}
      <mesh position={[0, LEG_H + 0.18, 0]} receiveShadow>
        <boxGeometry args={[PW - 0.10, 0.02, PD - 0.10]} />
        <meshStandardMaterial color="#9aabb8" metalness={0.3} roughness={0.7} />
      </mesh>
      {/* Bins on level 1 */}
      {[
        [-0.10, -0.10],
        [0.10, -0.10],
        [-0.10, 0.10],
        [0.10, 0.10],
      ].map(([bx, bz], i) => (
        <group key={`bin1-${i}`} position={[bx, LEG_H + 0.20, bz]}>
          <mesh position={[0, 0.05, 0]} castShadow>
            <boxGeometry args={[0.10, 0.09, 0.10]} />
            <meshStandardMaterial color={BIN_COLORS[i % 3]} metalness={0.05} roughness={0.85} />
          </mesh>
          <mesh position={[0, 0.095, 0]}>
            <boxGeometry args={[0.11, 0.008, 0.11]} />
            <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
          </mesh>
        </group>
      ))}

      {/* ═══ Shelf level 2 (top) ═══ */}
      <mesh position={[0, LEG_H + 0.36, 0]} receiveShadow>
        <boxGeometry args={[PW - 0.10, 0.02, PD - 0.10]} />
        <meshStandardMaterial color="#9aabb8" metalness={0.3} roughness={0.7} />
      </mesh>
      {/* Bins on level 2 */}
      {[
        [-0.10, 0],
        [0.10, 0],
      ].map(([bx, bz], i) => (
        <group key={`bin2-${i}`} position={[bx, LEG_H + 0.38, bz]}>
          <mesh position={[0, 0.05, 0]} castShadow>
            <boxGeometry args={[0.14, 0.09, 0.14]} />
            <meshStandardMaterial color={BIN_COLORS[(i + 1) % 3]} metalness={0.05} roughness={0.85} />
          </mesh>
          <mesh position={[0, 0.095, 0]}>
            <boxGeometry args={[0.15, 0.008, 0.15]} />
            <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
          </mesh>
        </group>
      ))}

      {/* ═══ Top rim / frame reinforcement ═══ */}
      <mesh position={[0, LEG_H + 0.44, 0]}>
        <boxGeometry args={[PW - 0.06, 0.015, PD - 0.06]} />
        <meshStandardMaterial color="#8a9aaa" metalness={0.4} roughness={0.6} transparent opacity={0.8} />
      </mesh>

      {/* ═══ Side cross braces ═══ */}
      {[-1, 1].map((side) => (
        <mesh key={`xbrace-${side}`} position={[side * hw, LEG_H + 0.18, 0]} rotation={[0, 0, 0.5]}>
          <boxGeometry args={[0.012, 0.25, 0.012]} />
          <meshStandardMaterial color="#8a9aaa" metalness={0.4} roughness={0.6} />
        </mesh>
      ))}
      {[-1, 1].map((side) => (
        <mesh key={`xbrace2-${side}`} position={[0, LEG_H + 0.18, side * hd]} rotation={[0.5, 0, 0]}>
          <boxGeometry args={[0.012, 0.25, 0.012]} />
          <meshStandardMaterial color="#8a9aaa" metalness={0.4} roughness={0.6} />
        </mesh>
      ))}

      {/* ═══ Label ═══ */}
      <Text position={[0, LEG_H + 0.52, 0]} fontSize={0.07} color="#5a6a7a" anchorX="center" anchorY="bottom">
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

          {shelvesWithContainers(layout.shelves, layout.storedContainers)}

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
