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
const HK_ORANGE = '#e8772e'
const HK_DARK = '#2a2a3a'
const HK_GRAY = '#4a4a5a'

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

// ─── Sub-component: Caster wheel (small, omnidirectional) ───────────
function Caster({ pos }: { pos: [number, number, number] }) {
  const cRadius = WHEEL_RADIUS * 0.55
  return (
    <group position={pos}>
      {/* Caster fork */}
      <mesh position={[0, cRadius * 0.9, 0]}>
        <cylinderGeometry args={[0.015, 0.02, cRadius * 1.8, 6]} />
        <meshStandardMaterial color={HK_GRAY} metalness={0.6} roughness={0.4} />
      </mesh>
      {/* Caster wheel */}
      <mesh position={[0, 0, 0]}>
        <sphereGeometry args={[cRadius, 8, 6]} />
        <meshStandardMaterial color={TIRE_COLOR} roughness={0.9} />
      </mesh>
    </group>
  )
}

// ─── Sub-component: Status Light Tower ──────────────────────────────
function LightTower({ status }: { status: RobotStatusType }) {
  const colors =
    status === 'ERROR'
      ? ['#e74c3c', '#e74c3c', '#e74c3c']
      : status === 'LOADING' || status === 'UNLOADING'
        ? ['#f5a623', '#f5a623', '#444']
        : status === 'MOVING'
          ? ['#27ae60', '#444', '#444']
          : ['#4a90d9', '#444', '#444']

  return (
    <group position={[0, 0.01, 0]}>
      {[0, 1, 2].map((i) => (
        <mesh key={`tower-${i}`} position={[0, 0.05 + i * 0.04, 0]}>
          <cylinderGeometry args={[0.015, 0.018, 0.03, 6]} />
          <meshStandardMaterial
            color={colors[i]}
            emissive={colors[i]}
            emissiveIntensity={colors[i] !== '#444' ? 0.8 : 0}
            transparent
            opacity={colors[i] !== '#444' ? 0.9 : 0.3}
          />
        </mesh>
      ))}
    </group>
  )
}

// ═══════════════════════════════════════════════════════════════════
// HIKVISION-INSPIRED BIN_ROBOT (flat-top bin-transport AGV)
// Ref: Hikvision MR-B series / bin-handling AGVs
// ═══════════════════════════════════════════════════════════════════
function BinRobotMesh({ robot, isSelected }: { robot: RobotState; isSelected: boolean }) {
  const color = STATUS_COLORS[robot.status]
  const glow = STATUS_GLOW[robot.status]
  const isMoving = robot.status.startsWith('MOVING')

  // Spin wheels when moving
  const wheelSpinRef = useRef(0)
  useFrame((_, delta) => {
    if (isMoving) wheelSpinRef.current += delta * 5
  })

  // ─── Body dimensions (elongated flat-top) ───
  const BW = 0.60 // body width
  const BH = 0.20 // body height
  const BD = 0.85 // body depth (longer than KIVA)

  return (
    <group>
      {/* ═══ Shadow disc ═══ */}
      <mesh position={[0, -ROBOT_Y + 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <circleGeometry args={[0.55, 24]} />
        <meshBasicMaterial color="#000" transparent opacity={0.12} depthWrite={false} />
      </mesh>

      {/* ═══ Main chassis – dark gray body ═══ */}
      <mesh position={[0, BH / 2, 0]} castShadow>
        <boxGeometry args={[BW, BH, BD]} />
        <meshStandardMaterial color={HK_DARK} metalness={0.5} roughness={0.5} />
      </mesh>

      {/* ═══ Orange safety stripe (wraps around body) ═══ */}
      <mesh position={[0, BH * 0.25, -BD / 2 - 0.005]}>
        <boxGeometry args={[BW + 0.01, 0.03, 0.008]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.08} />
      </mesh>
      <mesh position={[0, BH * 0.25, BD / 2 + 0.005]}>
        <boxGeometry args={[BW + 0.01, 0.03, 0.008]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.08} />
      </mesh>
      <mesh position={[-BW / 2 - 0.005, BH * 0.25, 0]}>
        <boxGeometry args={[0.008, 0.03, BD + 0.01]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.08} />
      </mesh>
      <mesh position={[BW / 2 + 0.005, BH * 0.25, 0]}>
        <boxGeometry args={[0.008, 0.03, BD + 0.01]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.08} />
      </mesh>

      {/* ═══ Top deck plate (raised platform) ═══ */}
      <mesh position={[0, BH - 0.005, 0]}>
        <boxGeometry args={[BW - 0.06, 0.02, BD - 0.06]} />
        <meshStandardMaterial color="#3a3a4a" metalness={0.6} roughness={0.3} />
      </mesh>

      {/* ═══ Roller conveyor section (top – for bin transfer) ═══ */}
      <group position={[0, BH + 0.015, 0]}>
        {/* Conveyor frame rails */}
        <mesh position={[-BW * 0.28, 0.015, 0]}>
          <boxGeometry args={[0.02, 0.03, BD - 0.20]} />
          <meshStandardMaterial color={HK_GRAY} metalness={0.6} roughness={0.4} />
        </mesh>
        <mesh position={[BW * 0.28, 0.015, 0]}>
          <boxGeometry args={[0.02, 0.03, BD - 0.20]} />
          <meshStandardMaterial color={HK_GRAY} metalness={0.6} roughness={0.4} />
        </mesh>
        {/* Conveyor rollers */}
        {Array.from({ length: 7 }).map((_, i) => {
          const rz = -BD * 0.30 + i * (BD * 0.10)
          return (
            <mesh key={`roller-${i}`} position={[0, 0.03, rz]} rotation={[0, 0, Math.PI / 2]}>
              <cylinderGeometry args={[0.015, 0.015, BW * 0.48, 8]} />
              <meshStandardMaterial color="#8888aa" metalness={0.7} roughness={0.3} />
            </mesh>
          )
        })}
      </group>

      {/* ═══ Side guide rails (for bin alignment) ═══ */}
      {[-1, 1].map((side) => (
        <mesh key={`guide-${side}`} position={[side * BW * 0.32, BH + 0.10, 0]} castShadow>
          <boxGeometry args={[0.025, 0.14, BD - 0.15]} />
          <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} />
        </mesh>
      ))}
      {/* Guide rail end caps */}
      {[-1, 1].map((side) => (
        <>
          <mesh key={`cap-f-${side}`} position={[side * BW * 0.32, BH + 0.10, -BD * 0.47]}>
            <boxGeometry args={[0.03, 0.16, 0.02]} />
            <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} />
          </mesh>
          <mesh key={`cap-r-${side}`} position={[side * BW * 0.32, BH + 0.10, BD * 0.47]}>
            <boxGeometry args={[0.03, 0.16, 0.02]} />
            <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} />
          </mesh>
        </>
      ))}

      {/* ═══ LiDAR sensor turret (front) ═══ */}
      <mesh position={[0, BH + 0.04, -BD * 0.20]}>
        <cylinderGeometry args={[0.025, 0.035, 0.04, 12]} />
        <meshStandardMaterial color={HK_GRAY} metalness={0.7} roughness={0.3} />
      </mesh>
      <mesh position={[0, BH + 0.07, -BD * 0.20]}>
        <cylinderGeometry args={[0.03, 0.03, 0.025, 16]} />
        <meshStandardMaterial color="#222233" metalness={0.8} roughness={0.2} />
      </mesh>
      <mesh position={[0, BH + 0.07, -BD * 0.20]}>
        <torusGeometry args={[0.028, 0.003, 8, 16]} />
        <meshBasicMaterial color="#00ffaa" transparent opacity={0.3} />
      </mesh>

      {/* ═══ Hikvision logo area (front decal) ═══ */}
      <mesh position={[0, BH * 0.55, -BD / 2 - 0.005]}>
        <planeGeometry args={[0.06, 0.04]} />
        <meshBasicMaterial color="#ffffff" transparent opacity={0.25} />
      </mesh>

      {/* ═══ Status light tower (rear top) ═══ */}
      <group position={[0, BH + 0.18, BD * 0.30]}>
        <mesh position={[0, 0.06, 0]}>
          <cylinderGeometry args={[0.012, 0.015, 0.12, 6]} />
          <meshStandardMaterial color={HK_GRAY} metalness={0.5} roughness={0.5} />
        </mesh>
        <LightTower status={robot.status} />
      </group>

      {/* ═══ Emergency stop button (red, front top) ═══ */}
      <mesh position={[0.18, BH + 0.015, -BD * 0.15]}>
        <cylinderGeometry args={[0.02, 0.025, 0.01, 12]} />
        <meshStandardMaterial color="#cc2222" emissive="#ff4444" emissiveIntensity={0.2} roughness={0.5} />
      </mesh>
      <mesh position={[0.18, BH + 0.015, -BD * 0.15]}>
        <torusGeometry args={[0.022, 0.004, 6, 12]} />
        <meshBasicMaterial color="#ffcc00" />
      </mesh>

      {/* ═══ Headlights (front) ═══ */}
      {[-0.15, 0.15].map((ox, i) => (
        <mesh key={`light-${i}`} position={[ox, 0.06, -BD / 2 - 0.01]}>
          <sphereGeometry args={[0.015, 8, 8]} />
          <meshStandardMaterial
            color={isMoving ? '#ffeecc' : '#444'}
            emissive={isMoving ? '#ffeeaa' : '#000'}
            emissiveIntensity={isMoving ? 0.6 : 0}
          />
        </mesh>
      ))}

      {/* ═══ Rear tail lights ═══ */}
      {[-0.15, 0.15].map((ox, i) => (
        <mesh key={`taillight-${i}`} position={[ox, 0.06, BD / 2 + 0.01]}>
          <sphereGeometry args={[0.01, 6, 6]} />
          <meshStandardMaterial
            color={isMoving ? '#ff4444' : '#333'}
            emissive={isMoving ? '#ff2222' : '#000'}
            emissiveIntensity={isMoving ? 0.3 : 0}
          />
        </mesh>
      ))}

      {/* ═══ Side status LED strips ═══ */}
      <mesh position={[BW / 2 + 0.005, BH / 2 + 0.02, -BD * 0.10]}>
        <boxGeometry args={[0.005, 0.04, 0.15]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>
      <mesh position={[-BW / 2 - 0.005, BH / 2 + 0.02, -BD * 0.10]}>
        <boxGeometry args={[0.005, 0.04, 0.15]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>

      {/* ═══ Battery indicator strip (side) ═══ */}
      {[-1, 1].map((side) => (
        <mesh key={`batt-${side}`} position={[side * BW * 0.49, 0.04, BD * 0.30]}>
          <boxGeometry args={[0.005, 0.03, 0.06]} />
          <meshStandardMaterial
            color={robot.batteryLevel > 0.3 ? '#27ae60' : '#e74c3c'}
            emissive={robot.batteryLevel > 0.3 ? '#27ae60' : '#e74c3c'}
            emissiveIntensity={0.4}
          />
        </mesh>
      ))}

      {/* ═══ Conveyor belt pattern (decorative strip on side) ═══ */}
      {Array.from({ length: 6 }).map((_, i) => (
        <mesh key={`belt-${i}`} position={[BW / 2 + 0.008, BH * 0.5, -BD * 0.25 + i * 0.08]}>
          <boxGeometry args={[0.003, 0.015, 0.02]} />
          <meshStandardMaterial color="#666688" metalness={0.6} roughness={0.4} />
        </mesh>
      ))}

      {/* ═══ Basket items (standing upright on conveyor) ═══ */}
      {robot.basketItems && robot.basketItems.length > 0 && (
        <group position={[0, BH + 0.08, -BD * 0.05]}>
          {robot.basketItems.slice(0, 3).map((_, idx) => (
            <group key={idx} position={[-0.12 + idx * 0.13, 0, 0]}>
              {/* Bin body – tall, standing upright */}
              <mesh position={[0, 0.07, 0]} castShadow>
                <boxGeometry args={[0.08, 0.14, 0.08]} />
                <meshStandardMaterial
                  color={['#b8b8b8', '#a8b8b0', '#b0b0b8'][idx % 3]}
                  metalness={0.05}
                  roughness={0.85}
                />
              </mesh>
              {/* Bin rim (top edge) */}
              <mesh position={[0, 0.142, 0]}>
                <boxGeometry args={[0.09, 0.008, 0.09]} />
                <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
              </mesh>
              {/* Front label panel */}
              <mesh position={[0, 0.06, 0.041]}>
                <planeGeometry args={[0.05, 0.05]} />
                <meshStandardMaterial color="#d0d0d0" metalness={0.0} roughness={0.9} />
              </mesh>
            </group>
          ))}
          {/* Side containment bars */}
          <mesh position={[-0.18, 0.09, 0]}>
            <boxGeometry args={[0.015, 0.16, 0.35]} />
            <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} transparent opacity={0.6} />
          </mesh>
          <mesh position={[0.18, 0.09, 0]}>
            <boxGeometry args={[0.015, 0.16, 0.35]} />
            <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} transparent opacity={0.6} />
          </mesh>
        </group>
      )}

      {/* ═══ Carried container (on top) ═══ */}
      {robot.carriedContainerCode && (
        <group position={[0, BH + 0.15, -BD * 0.05]}>
          <mesh position={[0, 0.10, 0]} castShadow>
            <boxGeometry args={[0.30, 0.18, 0.30]} />
            <meshStandardMaterial color="#b8b8b8" metalness={0.05} roughness={0.85} />
          </mesh>
          <mesh position={[0, 0.19, 0]}>
            <boxGeometry args={[0.32, 0.012, 0.32]} />
            <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
          </mesh>
        </group>
      )}

      {/* ═══ Wheels (2 drive + 4 casters) ═══ */}
      {/* Drive wheels (center) */}
      <Wheel pos={[-BW * 0.28, WHEEL_RADIUS, -BD * 0.15]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW * 0.28, WHEEL_RADIUS, -BD * 0.15]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[-BW * 0.28, WHEEL_RADIUS, BD * 0.15]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW * 0.28, WHEEL_RADIUS, BD * 0.15]} rotY={0} spin={wheelSpinRef.current} />

      {/* Corner casters */}
      <Caster pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.4, -BD * 0.42]} />
      <Caster pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.4, -BD * 0.42]} />
      <Caster pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.4, BD * 0.42]} />
      <Caster pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.4, BD * 0.42]} />

      {/* ═══ Corner bumpers (rubber) ═══ */}
      {[
        [-BW / 2, -BD / 2],
        [BW / 2, -BD / 2],
        [-BW / 2, BD / 2],
        [BW / 2, BD / 2],
      ].map(([bx, bz], i) => (
        <mesh key={`bumper-${i}`} position={[bx, 0.04, bz]}>
          <boxGeometry args={[0.05, 0.05, 0.05]} />
          <meshStandardMaterial color="#1a1a2a" metalness={0.2} roughness={0.9} />
        </mesh>
      ))}

      {/* ═══ Selection ring ═══ */}
      {isSelected && (
        <mesh position={[0, 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.52, 0.60, 32]} />
          <meshBasicMaterial color="#ffdd44" transparent opacity={0.7} />
        </mesh>
      )}

      {/* ═══ Label ═══ */}
      <Text position={[0, 0.40, 0]} fontSize={0.16} color="#1a2a3a" anchorX="center" anchorY="bottom">
        {robot.robotCode}
      </Text>
    </group>
  )
}

// ═══════════════════════════════════════════════════════════════════
// HIKVISION-INSPIRED KIVA AGV — dive-type (潜伏式) pod lifter
// Ref: Hikvision MR-1000L / dive-type AGV
// ═══════════════════════════════════════════════════════════════════
function KivaRobotMesh({ robot, isSelected }: { robot: RobotState; isSelected: boolean }) {
  const color = STATUS_COLORS[robot.status]
  const glow = STATUS_GLOW[robot.status]
  const isMoving = robot.status.startsWith('MOVING')

  // Lower, wider body for dive-type AGV
  const BW = 0.55
  const BH = 0.16
  const BD = 0.62

  // Spin wheels when moving
  const wheelSpinRef = useRef(0)
  useFrame((_, delta) => {
    if (isMoving) wheelSpinRef.current += delta * 5
  })

  return (
    <group>
      {/* ═══ Shadow disc ═══ */}
      <mesh position={[0, -ROBOT_Y + 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <circleGeometry args={[0.45, 24]} />
        <meshBasicMaterial color="#000" transparent opacity={0.12} depthWrite={false} />
      </mesh>

      {/* ═══ Main body – low-profile dark gray puck ═══ */}
      <mesh position={[0, BH / 2, 0]} castShadow>
        <boxGeometry args={[BW, BH, BD]} />
        <meshStandardMaterial color={HK_DARK} metalness={0.5} roughness={0.5} />
      </mesh>

      {/* ═══ Top plate (raised platform) ═══ */}
      <mesh position={[0, BH - 0.005, 0]}>
        <boxGeometry args={[BW - 0.04, 0.02, BD - 0.04]} />
        <meshStandardMaterial color="#3a3a4a" metalness={0.6} roughness={0.3} />
      </mesh>

      {/* ═══ Orange safety stripe (wraps around) ═══ */}
      <mesh position={[0, BH * 0.22, -BD / 2 - 0.005]}>
        <boxGeometry args={[BW + 0.01, 0.03, 0.008]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.1} />
      </mesh>
      <mesh position={[0, BH * 0.22, BD / 2 + 0.005]}>
        <boxGeometry args={[BW + 0.01, 0.03, 0.008]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.1} />
      </mesh>
      <mesh position={[-BW / 2 - 0.005, BH * 0.22, 0]}>
        <boxGeometry args={[0.008, 0.03, BD + 0.01]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.1} />
      </mesh>
      <mesh position={[BW / 2 + 0.005, BH * 0.22, 0]}>
        <boxGeometry args={[0.008, 0.03, BD + 0.01]} />
        <meshStandardMaterial color={HK_ORANGE} metalness={0.3} roughness={0.5} emissive={HK_ORANGE} emissiveIntensity={0.1} />
      </mesh>

      {/* ═══ Corner bumpers ═══ */}
      {[
        [-BW / 2, -BD / 2],
        [BW / 2, -BD / 2],
        [-BW / 2, BD / 2],
        [BW / 2, BD / 2],
      ].map(([bx, bz], i) => (
        <mesh key={`bumper-${i}`} position={[bx, 0.035, bz]}>
          <boxGeometry args={[0.06, 0.05, 0.06]} />
          <meshStandardMaterial color="#1a1a2a" metalness={0.2} roughness={0.9} />
        </mesh>
      ))}

      {/* ═══ LiDAR sensor (front) ═══ */}
      <mesh position={[0, BH + 0.02, -BD * 0.18]}>
        <cylinderGeometry args={[0.022, 0.03, 0.035, 12]} />
        <meshStandardMaterial color={HK_GRAY} metalness={0.7} roughness={0.3} />
      </mesh>
      <mesh position={[0, BH + 0.05, -BD * 0.18]}>
        <cylinderGeometry args={[0.025, 0.025, 0.02, 16]} />
        <meshStandardMaterial color="#222233" metalness={0.8} roughness={0.2} />
      </mesh>
      <mesh position={[0, BH + 0.05, -BD * 0.18]}>
        <torusGeometry args={[0.023, 0.003, 8, 16]} />
        <meshBasicMaterial color="#00ffaa" transparent opacity={0.3} />
      </mesh>

      {/* ═══ E-stop button (top) ═══ */}
      <mesh position={[0.16, BH + 0.01, -BD * 0.08]}>
        <cylinderGeometry args={[0.018, 0.022, 0.008, 12]} />
        <meshStandardMaterial color="#cc2222" emissive="#ff4444" emissiveIntensity={0.2} roughness={0.5} />
      </mesh>

      {/* ═══ Status LED ring (top perimeter) — neutral when idle ═══ */}
      <mesh position={[0, BH + 0.003, 0]}>
        <torusGeometry args={[BW * 0.38, 0.01, 8, 24]} />
        <meshStandardMaterial color="#444" emissive={color} emissiveIntensity={glow} />
      </mesh>

      {/* ═══ Scissor lift mechanism (top – orange, prominent) ═══ */}
      <group position={[0, BH + 0.015, 0]}>
        {/* Lifting platform base rails */}
        {[-0.14, 0.14].map((ox, i) => (
          <mesh key={`lift-rail-${i}`} position={[ox, 0.008, 0]}>
            <boxGeometry args={[0.025, 0.015, BD * 0.55]} />
            <meshStandardMaterial color={HK_ORANGE} metalness={0.5} roughness={0.5} />
          </mesh>
        ))}
        {/* Cross braces (scissor arms) */}
        {[-1, 1].map((side, si) => (
          <mesh key={`scissor-${si}`} position={[side * 0.06, 0.02, BD * 0.10]} rotation={[0, 0, side * 0.45]}>
            <boxGeometry args={[0.015, 0.08, 0.015]} />
            <meshStandardMaterial color="#556677" metalness={0.7} roughness={0.3} />
          </mesh>
        ))}
        {[-1, 1].map((side, si) => (
          <mesh key={`scissor2-${si}`} position={[side * 0.06, 0.02, -BD * 0.10]} rotation={[0, 0, side * 0.45]}>
            <boxGeometry args={[0.015, 0.08, 0.015]} />
            <meshStandardMaterial color="#556677" metalness={0.7} roughness={0.3} />
          </mesh>
        ))}
        {/* Lift platform top plate (thin) */}
        <mesh position={[0, 0.03, 0]}>
          <boxGeometry args={[0.34, 0.012, BD * 0.5]} />
          <meshStandardMaterial color={HK_ORANGE} metalness={0.4} roughness={0.5} />
        </mesh>
        {/* Lift guide pins */}
        {[-0.12, 0.12].map((ox, i) => (
          <mesh key={`pin-${i}`} position={[ox, 0.04, 0]}>
            <cylinderGeometry args={[0.006, 0.008, 0.015, 6]} />
            <meshBasicMaterial color="#ffcc00" />
          </mesh>
        ))}
      </group>

      {/* ═══ Hikvision logo area ═══ */}
      <mesh position={[0.08, BH + 0.01, 0.13]}>
        <planeGeometry args={[0.05, 0.025]} />
        <meshBasicMaterial color="#ffffff" transparent opacity={0.15} />
      </mesh>

      {/* ═══ Direction indicator chevron (front) ═══ */}
      <group position={[0, BH / 2, -BD / 2 - 0.015]}>
        <mesh position={[0, 0, 0]} rotation={[(isMoving ? 0.3 : 0), 0, 0]}>
          <boxGeometry args={[0.10, 0.01, 0.04]} />
          <meshStandardMaterial
            color={isMoving ? '#ffffff' : '#555'}
            emissive={isMoving ? '#ffffff' : '#000'}
            emissiveIntensity={isMoving ? 0.5 : 0}
          />
        </mesh>
        <mesh position={[0, 0.005, -0.025]} rotation={[(isMoving ? 0.3 : 0), 0, 0]}>
          <coneGeometry args={[0.035, 0.04, 3]} />
          <meshStandardMaterial
            color={isMoving ? '#ffffff' : '#555'}
            emissive={isMoving ? '#ffffff' : '#000'}
            emissiveIntensity={isMoving ? 0.5 : 0}
          />
        </mesh>
      </group>

      {/* ═══ Drive wheels (center) ═══ */}
      <Wheel pos={[-BW * 0.28, WHEEL_RADIUS * 0.8, -BD * 0.12]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW * 0.28, WHEEL_RADIUS * 0.8, -BD * 0.12]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[-BW * 0.28, WHEEL_RADIUS * 0.8, BD * 0.12]} rotY={0} spin={wheelSpinRef.current} />
      <Wheel pos={[BW * 0.28, WHEEL_RADIUS * 0.8, BD * 0.12]} rotY={0} spin={wheelSpinRef.current} />

      {/* Corner casters */}
      <Caster pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.35, -BD * 0.42]} />
      <Caster pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.35, -BD * 0.42]} />
      <Caster pos={[-BW / 2 - 0.02, WHEEL_RADIUS * 0.35, BD * 0.42]} />
      <Caster pos={[BW / 2 + 0.02, WHEEL_RADIUS * 0.35, BD * 0.42]} />

      {/* ═══ Headlights (front) ═══ */}
      {[-0.12, 0.12].map((ox, i) => (
        <mesh key={`light-${i}`} position={[ox, 0.05, -BD / 2 - 0.01]}>
          <sphereGeometry args={[0.015, 8, 8]} />
          <meshStandardMaterial
            color={isMoving ? '#ffeecc' : '#444'}
            emissive={isMoving ? '#ffeeaa' : '#000'}
            emissiveIntensity={isMoving ? 0.6 : 0}
          />
        </mesh>
      ))}

      {/* ═══ Rear tail lights ═══ */}
      {[-0.12, 0.12].map((ox, i) => (
        <mesh key={`taillight-${i}`} position={[ox, 0.05, BD / 2 + 0.01]}>
          <sphereGeometry args={[0.01, 6, 6]} />
          <meshStandardMaterial
            color={isMoving ? '#ff4444' : '#333'}
            emissive={isMoving ? '#ff2222' : '#000'}
            emissiveIntensity={isMoving ? 0.3 : 0}
          />
        </mesh>
      ))}

      {/* ═══ Side battery indicator ═══ */}
      {[-1, 1].map((side) => (
        <mesh key={`batt-${side}`} position={[side * BW * 0.49, 0.035, BD * 0.20]}>
          <boxGeometry args={[0.005, 0.02, 0.05]} />
          <meshStandardMaterial
            color={robot.batteryLevel > 0.3 ? '#27ae60' : '#e74c3c'}
            emissive={robot.batteryLevel > 0.3 ? '#27ae60' : '#e74c3c'}
            emissiveIntensity={0.4}
          />
        </mesh>
      ))}

      {/* ═══ Side status LED strips ═══ */}
      <mesh position={[BW / 2 + 0.005, BH / 2 + 0.02, -BD * 0.08]}>
        <boxGeometry args={[0.005, 0.03, 0.12]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>
      <mesh position={[-BW / 2 - 0.005, BH / 2 + 0.02, -BD * 0.08]}>
        <boxGeometry args={[0.005, 0.03, 0.12]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={glow} />
      </mesh>

      {/* ═══ Carrying pod (料架) – lightweight shelving ═══ */}
      {robot.carryingPodId && (
        <group position={[0, BH + 0.06, 0]}>
          {/* Pod base platform */}
          <mesh position={[0, 0.03, 0]} castShadow>
            <boxGeometry args={[0.50, 0.04, 0.50]} />
            <meshStandardMaterial color="#8a9aaa" metalness={0.4} roughness={0.6} />
          </mesh>
          {/* Pod corner legs */}
          {[
            [-0.20, -0.20],
            [0.20, -0.20],
            [-0.20, 0.20],
            [0.20, 0.20],
          ].map(([px, pz], i) => (
            <mesh key={`podleg-${i}`} position={[px, 0.08, pz]}>
              <cylinderGeometry args={[0.018, 0.022, 0.06, 6]} />
              <meshStandardMaterial color="#6a7a8a" metalness={0.6} roughness={0.5} />
            </mesh>
          ))}
          {/* Pod shelf level 1 */}
          <mesh position={[0, 0.12, 0]} receiveShadow>
            <boxGeometry args={[0.46, 0.02, 0.46]} />
            <meshStandardMaterial color="#9aabb8" metalness={0.3} roughness={0.7} />
          </mesh>
          {/* Bins on shelf 1 */}
          {[
            [-0.10, -0.10],
            [0.10, -0.10],
            [-0.10, 0.10],
            [0.10, 0.10],
          ].map(([bx, bz], i) => (
            <group key={`carry-bin1-${i}`} position={[bx, 0.14, bz]}>
              <mesh position={[0, 0.04, 0]} castShadow>
                <boxGeometry args={[0.10, 0.07, 0.10]} />
                <meshStandardMaterial color={['#b8b8b8', '#a8b8b0', '#b0b0b8', '#b8b0a8'][i]} metalness={0.05} roughness={0.85} />
              </mesh>
              <mesh position={[0, 0.075, 0]}>
                <boxGeometry args={[0.105, 0.006, 0.105]} />
                <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
              </mesh>
            </group>
          ))}
          {/* Pod shelf level 2 */}
          <mesh position={[0, 0.24, 0]} receiveShadow>
            <boxGeometry args={[0.46, 0.02, 0.46]} />
            <meshStandardMaterial color="#9aabb8" metalness={0.3} roughness={0.7} />
          </mesh>
          {/* Bins on shelf 2 */}
          {[
            [-0.08, 0],
            [0.08, 0],
          ].map(([bx, bz], i) => (
            <group key={`carry-bin2-${i}`} position={[bx, 0.26, bz]}>
              <mesh position={[0, 0.04, 0]} castShadow>
                <boxGeometry args={[0.14, 0.07, 0.14]} />
                <meshStandardMaterial color={['#b0b0b8', '#aab0b0'][i]} metalness={0.05} roughness={0.85} />
              </mesh>
              <mesh position={[0, 0.075, 0]}>
                <boxGeometry args={[0.145, 0.006, 0.145]} />
                <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
              </mesh>
            </group>
          ))}
          {/* Orange latch mechanism */}
          {[-0.12, 0.12].map((ox, i) => (
            <mesh key={`latch-${i}`} position={[ox, 0.005, 0]}>
              <boxGeometry args={[0.025, 0.015, 0.025]} />
              <meshStandardMaterial color={HK_ORANGE} metalness={0.5} roughness={0.4} />
            </mesh>
          ))}
        </group>
      )}

      {/* ═══ Carried container (on lift platform) ═══ */}
      {robot.carriedContainerCode && !robot.carryingPodId && (
        <group position={[0, BH + 0.06, 0]}>
          <mesh position={[0, 0.10, 0]} castShadow>
            <boxGeometry args={[0.30, 0.18, 0.30]} />
            <meshStandardMaterial color="#b8b8b8" metalness={0.05} roughness={0.85} />
          </mesh>
          <mesh position={[0, 0.19, 0]}>
            <boxGeometry args={[0.32, 0.012, 0.32]} />
            <meshStandardMaterial color="#c0c0c0" metalness={0.1} roughness={0.8} />
          </mesh>
        </group>
      )}

      {/* ═══ Selection ring ═══ */}
      {isSelected && (
        <mesh position={[0, 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.48, 0.55, 32]} />
          <meshBasicMaterial color="#ffdd44" transparent opacity={0.7} />
        </mesh>
      )}

      {/* ═══ Label ═══ */}
      <Text position={[0, 0.35, 0]} fontSize={0.14} color="#1a2a3a" anchorX="center" anchorY="bottom">
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
