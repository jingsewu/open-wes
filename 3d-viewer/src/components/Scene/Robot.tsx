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

const STATUS_COLORS: Record<RobotStatusType, string> = {
  IDLE: '#4a90d9',
  MOVING_TO_PICKUP: '#27ae60',
  LOADING: '#f39c12',
  MOVING_TO_DESTINATION: '#27ae60',
  UNLOADING: '#f39c12',
  CHARGING: '#95a5a6',
  ERROR: '#e74c3c',
}

const ROBOT_Y = 0.25
const LERP_FACTOR = 0.15

export function Robot({ robot, isSelected, onClick }: RobotProps) {
  const groupRef = useRef<THREE.Group>(null)

  useFrame(() => {
    if (!groupRef.current) return
    const targetX = robot.x
    const targetZ = robot.y
    groupRef.current.position.x = THREE.MathUtils.lerp(groupRef.current.position.x, targetX, LERP_FACTOR)
    groupRef.current.position.z = THREE.MathUtils.lerp(groupRef.current.position.z, targetZ, LERP_FACTOR)

    const targetRotation = (-robot.rotation * Math.PI) / 180
    groupRef.current.rotation.y = THREE.MathUtils.lerp(groupRef.current.rotation.y, targetRotation, LERP_FACTOR)
  })

  const color = STATUS_COLORS[robot.status]
  const isForklift = robot.robotType === 'FORKLIFT'
  const bodyWidth = isForklift ? 0.8 : 0.6
  const bodyDepth = isForklift ? 1.0 : 0.6

  return (
    <group ref={groupRef} position={[robot.x, ROBOT_Y, robot.y]} onClick={onClick}>
      {/* Robot body */}
      <mesh castShadow>
        <boxGeometry args={[bodyWidth, 0.3, bodyDepth]} />
        <meshStandardMaterial color={color} />
      </mesh>

      {/* Direction indicator */}
      <mesh position={[0, 0.2, -bodyDepth / 2 + 0.1]}>
        <coneGeometry args={[0.1, 0.2, 4]} />
        <meshStandardMaterial color="#fff" />
      </mesh>

      {/* Status LED */}
      <mesh position={[0, 0.25, 0]}>
        <sphereGeometry args={[0.06]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={0.8} />
      </mesh>

      {/* Selection ring */}
      {isSelected && (
        <mesh position={[0, 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.5, 0.6, 32]} />
          <meshBasicMaterial color="#fff200" />
        </mesh>
      )}

      {/* Carried container */}
      {robot.carriedContainerCode && (
        <mesh position={[0, 0.35, 0]} castShadow>
          <boxGeometry args={[0.5, 0.3, 0.5]} />
          <meshStandardMaterial color="#e67e22" />
        </mesh>
      )}

      {/* Label */}
      <Text
        position={[0, 0.6, 0]}
        fontSize={0.2}
        color="#333"
        anchorX="center"
        anchorY="bottom"
      >
        {robot.robotCode}
      </Text>
    </group>
  )
}
