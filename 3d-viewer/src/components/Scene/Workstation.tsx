import { Text } from '@react-three/drei'
import type { WorkstationConfig } from '@/types'

interface WorkstationProps {
  config: WorkstationConfig
}

const WS_COLORS: Record<string, string> = {
  PICKING: '#2ecc71',
  RECEIVING: '#3498db',
}

export function Workstation({ config }: WorkstationProps) {
  const color = WS_COLORS[config.type] || '#9b59b6'

  return (
    <group position={[config.x, 0.4, config.y]}>
      {/* Workstation desk */}
      <mesh castShadow>
        <boxGeometry args={[1.5, 0.8, 1.5]} />
        <meshStandardMaterial color={color} opacity={0.9} transparent />
      </mesh>
      {/* Label */}
      <Text
        position={[0, 1.0, 0]}
        fontSize={0.3}
        color="#333"
        anchorX="center"
        anchorY="bottom"
      >
        {config.id}
      </Text>
      <Text
        position={[0, 0.7, 0]}
        fontSize={0.18}
        color="#666"
        anchorX="center"
        anchorY="bottom"
      >
        {config.type}
      </Text>
    </group>
  )
}
