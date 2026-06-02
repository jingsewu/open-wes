import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Text } from '@react-three/drei'
import * as THREE from 'three'
import type { RobotState, RobotStatusType } from '@/types'

interface RobotProps {
  robot: RobotState
  isSelected: boolean
  onClick: () => void
}

// ─── Status colour palette ──────────────────────────────────────────
const STATUS_COLORS: Record<RobotStatusType, string> = {
  IDLE: '#4a90d9',
  MOVING: '#27ae60',
  LOADING: '#f5a623',
  UNLOADING: '#f5a623',
  WAITING: '#9b59b6',
  CHARGING: '#95a5a6',
  ERROR: '#e74c3c',
}

const STATUS_GLOW: Record<RobotStatusType, number> = {
  IDLE: 0.15,
  MOVING: 0.6,
  LOADING: 0.5,
  UNLOADING: 0.5,
  WAITING: 0.4,
  CHARGING: 0.08,
  ERROR: 1.0,
}

// ─── Shared constants ───────────────────────────────────────────────
const ROBOT_Y = 0.15
const LERP_FACTOR = 0.15
const WHEEL_RADIUS = 0.06
const WHEEL_WIDTH = 0.05
const TIRE_COLOR = '#1a1a2e'
const HUB_COLOR = '#444466'

// ─── Sub-component: Wheel ───────────────────────────────────────────
function Wheel({ pos, rotY = 0, spin = 0 }: { pos: [number, number, number]; rotY?: number; spin?: number }) {
  return (
    <group position={pos} rotation={[0, rotY, 0]}>
      {/* Tire */}
      <mesh rotation={[spin, 0, 0]} castShadow>
        <cylinderGeometry args={[WHEEL_RADIUS, WHEEL_RADIUS, WHEEL_WIDTH, 12]} />
        <meshStandardMaterial color={TIRE_COLOR} roughness={0.9} metalness={0.0} />
      </mesh>
      {/* Hub */}
      <mesh rotation={[spin, 0, 0]} position={[0, 0, WHEEL_WIDTH * 0.52]}>
        <cylinderGeometry args={[WHEEL_RADIUS * 0.35, WHEEL_RADIUS * 0.35, 0.01, 8]} />
        <meshStandardMaterial color={HUB_COLOR} metalness={0.7} roughness={0.4} />
      </mesh>
    </group>
  )
}

// ─── Sub-component: Headlight ───────────────────────────────────────
function Headlight({ on = true }: { on?: boolean }) {
  return (
    <mesh position={[0, 0.06, -0.31]}>
      <sphereGeometry args={[0.025, 8, 8]} />
      <meshStandardMaterial
        color={on ? '#ffeecc' : '#444'}
        emissive={on ? '#ffeeaa' : '#000'}
        emissiveIntensity={on ? 0.8 : 0}
      />
    </mesh>
  )
}

// ═══════════════════════════════════════════════════════════════════
//  BIN_ROBOT DESIGN (forklift + basket carrier)
// ═══════════════════════════════════════════════════════════════════
function BinRobotMesh({ robot, isSelected }: { robot: RobotState; isSelected: boolean }) {
  const color = STATUS_COLORS[robot.status]
  const glow = STATUS_GLOW[robot.status]
  const isMoving = robot.status.startsWith('MOVING')

  // Spin wheels when moving
  const wheelSpinRef = useRef(0)
  useFrame((_, delta) => {
    if (isMoving) wheelSpinRef.current += delta * 6
  })

  // ─── Body dimensions ───
  const BW = 0.55 // body width
  const BH = 0.20 // body height
  const BD = 0.65 // body depth
  const mastH = 0.55

  return (
    <group>
      {/* ═══ Shadow disc ═══ */}
      <mesh position={[0, -ROBOT_Y + 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <circleGeometry args={[0.45, 24]} />
        <meshBasicMaterial color="#000" transparent opacity={0.15} depthWrite={false} />
      </mesh>

      {/* ═══ Chassis (lower body) ═══ */}
      <mesh position={[0, BH / 2, 0]} castShadow>
        <boxGeometry args={[BW, BH, BD]} />
        <meshStandardMaterial color={color} metalness={0.5} roughness={0.4} />
      </mesh>

      {/* ═══ Cabin / upper body ═══ */}
      <mesh position={[0.06, BH + 0.12, -0.05]} castShadow>
        <boxGeometry args={[BW * 0.75, 0.18, BD * 0.55]} />
        <meshStandardMaterial color={color} metalness={0.4} roughness={0.5} />
      </mesh>

      {/* ═══ Overhead guard (4 posts + roof) ═══ */}
      {[
        [-BW * 0.32, BD * 0.2],
        [BW * 0.32, BD * 0.2],
        [-BW * 0.32, -BD * 0.28],
        [BW * 0.32, -BD * 0.28],
      ].map(([px, pz], i) => (
        <mesh key={`guard-${i}`} position={[px, BH + 0.32, pz]}>
          <cylinderGeometry args={[0.012, 0.012, 0.28, 6]} />
          <meshStandardMaterial color="#555566" metalness={0.6} roughness={0.4} />
        </mesh>
      ))}
      {/* Roof */}
      <mesh position={[0, BH + 0.46, -0.02]} castShadow>
        <boxGeometry args={[BW * 0.75, 0.02, BD * 0.6]} />
        <meshStandardMaterial color="#444455" metalness={0.5} roughness={0.5} transparent opacity={0.4} />
      </mesh>

      {/* ═══ Mast (vertical rails at front) ═══ */}
      {[-0.08, 0.08].map((ox, i) => (
        <mesh key={`mast-${i}`} position={[ox, BH + mastH / 2, -BD / 2 + 0.04]}>
          <boxGeometry args={[0.03, mastH, 0.03]} />
          <meshStandardMaterial color="#556677" metalness={0.7} roughness={0.3} />
        </mesh>
      ))}
      {/* Mast cross brace */}
      <mesh position={[0, BH + mastH * 0.4, -BD / 2 + 0.04]}>
        <boxGeometry args={[0.2, 0.02, 0.02]} />
        <meshStandardMaterial color="#556677" metalness={0.6} roughness={0.4} />
      </mesh>
      <mesh position={[0, BH + mastH * 0.7, -BD / 2 + 0.04]}>
        <boxGeometry args={[0.2, 0.02, 0.02]} />
        <meshStandardMaterial color="#556677" metalness={0.6} roughness={0.4} />
      </mesh>

      {/* ═══ Forks ═══ */}
      {[-0.08, 0.08].map((ox, i) => (
        <group key={`fork-${i}`}>
          {/* Horizontal prong */}
          <mesh position={[ox, 0.03, -BD / 2 - 0.18]} castShadow>
            <boxGeometry args={[0.04, 0.025, 0.3]} />
            <meshStandardMaterial color="#dd8833" metalness={0.6} roughness={0.5} />
          </mesh>
          {/* Heel (vertical bit) */}
          <mesh position={[ox, 0.06, -BD / 2 + 0.02]}>
            <boxGeometry args={[0.04, 0.08, 0.04]} />
            <meshStandardMaterial color="#cc7722" metalness={0.6} roughness={0.5} />
          </mesh>
        </group>
      ))}

      {/* ═══ Basket (rear-mounted for bins) ═══ */}
      {robot.basketItems && robot.basketItems.length > 0 && (
        <group position={[0, BH + 0.15, BD * 0.15]}>
          {/* Basket cage */}
          <mesh>
            <boxGeometry args={[0.35, 0.2, 0.25]} />
            <meshStandardMaterial color="#777788" metalness={0.3} roughness={0.6} transparent opacity={0.35} />
          </mesh>
          {/* Basket wireframe edges */}
          <mesh>
            <edgesGeometry args={[new THREE.BoxGeometry(0.35, 0.2, 0.25)]} />
            <lineBasicMaterial color="#9999aa" />
          </mesh>
          {/* Bin items inside */}
          {robot.basketItems.slice(0, 3).map((_, idx) => (
            <mesh key={idx} position={[-0.08 + idx * 0.08, -0.02, -0.04]}>
              <boxGeometry args={[0.05, 0.08, 0.05]} />
              <meshStandardMaterial
                color={['#d4832a', '#5a8f4a', '#4a7aaa'][idx % 3]}
                metalness={0.2}
                roughness={0.7}
              />
            </mesh>
          ))}
        </group>
      )}

      {/* ═══ Wheels ═══ */}
      {/* Drive wheels (front) */}
      <Wheel pos={[-BW / 2 - 0.01, WHEEL_RADIUS, -BD * 0.25]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW / 2 + 0.01, WHEEL_RADIUS, -BD * 0.25]} rotY={0} spin={wheelSpinRef.current} />
      {/* Steer wheels (rear) */}
      <Wheel pos={[-BW / 2 - 0.01, WHEEL_RADIUS, BD * 0.25]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW / 2 + 0.01, WHEEL_RADIUS, BD * 0.25]} rotY={0} spin={wheelSpinRef.current} />

      {/* ═══ Counterweight (rear) ═══ */}
      <mesh position={[0, 0.06, BD / 2 - 0.02]} castShadow>
        <boxGeometry args={[BW * 0.8, 0.1, 0.08]} />
        <meshStandardMaterial color="#333344" metalness={0.7} roughness={0.4} />
      </mesh>

      {/* ═══ Headlight ═══ */}
      <Headlight on={isMoving} />

      {/* ═══ Status LED strip (side) ═══ */}
      <mesh position={[BW / 2 + 0.005, BH / 2 + 0.02, -BD * 0.15]}>
        <boxGeometry args={[0.005, 0.04, 0.15]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>
      <mesh position={[-BW / 2 - 0.005, BH / 2 + 0.02, -BD * 0.15]}>
        <boxGeometry args={[0.005, 0.04, 0.15]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>

      {/* ═══ Selection ring ═══ */}
      {isSelected && (
        <mesh position={[0, 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.48, 0.55, 32]} />
          <meshBasicMaterial color="#ffdd44" transparent opacity={0.7} />
        </mesh>
      )}

      {/* ═══ Carried container ═══ */}
      {robot.carriedContainerCode && (
        <mesh position={[0, 0.45, -BD / 4]} castShadow>
          <boxGeometry args={[0.4, 0.25, 0.4]} />
          <meshStandardMaterial color="#d4832a" metalness={0.3} roughness={0.6} />
        </mesh>
      )}

      {/* ═══ Label ═══ */}
      <Text position={[0, 0.65, 0]} fontSize={0.16} color="#1a2a3a" anchorX="center" anchorY="bottom">
        {robot.robotCode}
      </Text>
    </group>
  )
}

// ═══════════════════════════════════════════════════════════════════
// KIVA AGV DESIGN (pod lifter)
// ═══════════════════════════════════════════════════════════════════
function KivaRobotMesh({ robot, isSelected }: { robot: RobotState; isSelected: boolean }) {
  const color = STATUS_COLORS[robot.status]
  const glow = STATUS_GLOW[robot.status]
  const isMoving = robot.status.startsWith('MOVING')

  const BW = 0.5
  const BH = 0.18
  const BD = 0.55

  return (
    <group>
      {/* ═══ Shadow disc ═══ */}
      <mesh position={[0, -ROBOT_Y + 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <circleGeometry args={[0.4, 24]} />
        <meshBasicMaterial color="#000" transparent opacity={0.12} depthWrite={false} />
      </mesh>

      {/* ═══ Main body ═══ */}
      <mesh position={[0, BH / 2, 0]} castShadow>
        <boxGeometry args={[BW, BH, BD]} />
        <meshStandardMaterial color={color} metalness={0.4} roughness={0.5} />
      </mesh>
      {/* Top chamfer plates */}
      <mesh position={[0, BH - 0.01, 0]}>
        <boxGeometry args={[BW - 0.06, 0.02, BD - 0.06]} />
        <meshStandardMaterial color={color} metalness={0.5} roughness={0.3} />
      </mesh>

      {/* ═══ Corner bumpers ═══ */}
      {[
        [-BW / 2, -BD / 2],
        [BW / 2, -BD / 2],
        [-BW / 2, BD / 2],
        [BW / 2, BD / 2],
      ].map(([bx, bz], i) => (
        <mesh key={`bumper-${i}`} position={[bx, 0.04, bz]}>
          <boxGeometry args={[0.06, 0.06, 0.06]} />
          <meshStandardMaterial color="#222233" metalness={0.3} roughness={0.8} />
        </mesh>
      ))}

      {/* ═══ Sensor dome / turret ═══ */}
      <mesh position={[0.05, BH + 0.06, -BD * 0.1]}>
        <sphereGeometry args={[0.08, 12, 10, 0, Math.PI * 2, 0, Math.PI / 2]} />
        <meshStandardMaterial color="#333344" metalness={0.6} roughness={0.3} />
      </mesh>
      {/* Sensor lens */}
      <mesh position={[0.05, BH + 0.1, -BD * 0.1]}>
        <sphereGeometry args={[0.02, 8, 8]} />
        <meshStandardMaterial color="#00ddff" emissive="#00ddff" emissiveIntensity={0.4} />
      </mesh>

      {/* ═══ Status LED ring (top edge) ═══ */}
      <mesh position={[0, BH + 0.005, 0]}>
        <torusGeometry args={[BW * 0.4, 0.012, 8, 24]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>

      {/* ═══ Direction arrow (front) ═══ */}
      <mesh position={[0, BH / 2, -BD / 2 - 0.02]} rotation={[(isMoving ? 0.3 : 0), 0, 0]}>
        <boxGeometry args={[0.08, 0.01, 0.06]} />
        <meshStandardMaterial
          color={isMoving ? '#ffffff' : '#666'}
          emissive={isMoving ? '#ffffff' : '#000'}
          emissiveIntensity={isMoving ? 0.5 : 0}
        />
      </mesh>
      {/* Arrow triangle */}
      <mesh position={[0, BH / 2 + 0.005, -BD / 2 - 0.03]}>
        <coneGeometry args={[0.03, 0.04, 3]} />
        <meshStandardMaterial
          color={isMoving ? '#ffffff' : '#666'}
          emissive={isMoving ? '#ffffff' : '#000'}
          emissiveIntensity={isMoving ? 0.5 : 0}
        />
      </mesh>

      {/* ═══ Wheels ═══ */}
      <Wheel pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.8, -BD * 0.25]} rotY={0} />
      <Wheel pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.8, -BD * 0.25]} rotY={0} />
      <Wheel pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.8, BD * 0.25]} rotY={0} />
      <Wheel pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.8, BD * 0.25]} rotY={0} />

      {/* ═══ Front headlights ═══ */}
      {[-0.1, 0.1].map((ox, i) => (
        <mesh key={`light-${i}`} position={[ox, 0.06, -BD / 2 - 0.01]}>
          <sphereGeometry args={[0.015, 8, 8]} />
          <meshStandardMaterial
            color={isMoving ? '#ffeecc' : '#444'}
            emissive={isMoving ? '#ffeeaa' : '#000'}
            emissiveIntensity={isMoving ? 0.6 : 0}
          />
        </mesh>
      ))}

      {/* ═══ Carrying pod (KIVA lifts entire pod) ═══ */}
      {robot.carryingPodId && (
        <mesh position={[0, BH + 0.12, 0]} castShadow>
          <boxGeometry args={[0.3, 0.12, 0.3]} />
          <meshStandardMaterial color="#7a9a5a" metalness={0.2} roughness={0.7} />
          {/* Pod legs */}
          {[
            [-0.1, -0.1],
            [0.1, -0.1],
            [-0.1, 0.1],
            [0.1, 0.1],
          ].map(([px, pz], i) => (
            <mesh key={`podleg-${i}`} position={[px, -0.08, pz]}>
              <cylinderGeometry args={[0.015, 0.02, 0.04, 6]} />
              <meshStandardMaterial color="#5a7a3a" metalness={0.3} roughness={0.6} />
            </mesh>
          ))}
        </mesh>
      )}

      {/* ═══ Carried container ═══ */}
      {robot.carriedContainerCode && (
        <mesh position={[0, 0.35, 0]} castShadow>
          <boxGeometry args={[0.35, 0.2, 0.35]} />
          <meshStandardMaterial color="#d4832a" metalness={0.3} roughness={0.6} />
        </mesh>
      )}

      {/* ═══ Selection ring ═══ */}
      {isSelected && (
        <mesh position={[0, 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.42, 0.5, 32]} />
          <meshBasicMaterial color="#ffdd44" transparent opacity={0.7} />
        </mesh>
      )}

      {/* ═══ Label ═══ */}
      <Text position={[0, 0.5, 0]} fontSize={0.14} color="#1a2a3a" anchorX="center" anchorY="bottom">
        {robot.robotCode}
      </Text>
    </group>
  )
}

// ═══════════════════════════════════════════════════════════════════
//  ROOT EXPORT
// ═══════════════════════════════════════════════════════════════════
export function Robot({ robot, isSelected, onClick }: RobotProps) {
  const groupRef = useRef<THREE.Group>(null)
  const isError = robot.status === 'ERROR'
  const isCharging = robot.status === 'CHARGING'
  const isBinRobot = robot.robotType === 'BIN_ROBOT'

  // ─── Smooth position lerp ───
  useFrame(() => {
    if (!groupRef.current) return
    groupRef.current.position.x = THREE.MathUtils.lerp(
      groupRef.current.position.x,
      robot.x,
      LERP_FACTOR,
    )
    groupRef.current.position.z = THREE.MathUtils.lerp(
      groupRef.current.position.z,
      robot.y,
      LERP_FACTOR,
    )
    const targetRot = (-robot.rotation * Math.PI) / 180
    groupRef.current.rotation.y = THREE.MathUtils.lerp(
      groupRef.current.rotation.y,
      targetRot,
      LERP_FACTOR,
    )
  })

  return (
    <group
      ref={groupRef}
      position={[robot.x, ROBOT_Y, robot.y]}
      onClick={(e) => {
        e.stopPropagation()
        onClick()
      }}
    >
      {/* ═══ Error glow ═══ */}
      {isError && (
        <mesh position={[0, 0.3, 0]}>
          <sphereGeometry args={[0.15, 12, 12]} />
          <meshBasicMaterial color="#ff3333" transparent opacity={0.15} depthWrite={false} />
        </mesh>
      )}

      {/* ═══ Charging sparkle ═══ */}
      {isCharging && (
        <mesh position={[0, 0.35, 0]}>
          <sphereGeometry args={[0.05, 8, 8]} />
          <meshBasicMaterial color="#00ddff" transparent opacity={0.4} depthWrite={false} />
        </mesh>
      )}

      {/* Render the appropriate body */}
      {isBinRobot ? (
        <BinRobotMesh robot={robot} isSelected={isSelected} />
      ) : (
        <KivaRobotMesh robot={robot} isSelected={isSelected} />
      )}
    </group>
  )
}
