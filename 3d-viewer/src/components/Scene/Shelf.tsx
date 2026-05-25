import { Text } from '@react-three/drei'
import type { ShelfConfig } from '@/types'

interface ShelfProps {
  config: ShelfConfig
}

const SHELF_HEIGHT = 2.5
const SHELF_COLOR = '#5b7fa5'

export function Shelf({ config }: ShelfProps) {
  return (
    <group position={[config.x + config.width / 2, SHELF_HEIGHT / 2, config.y + config.height / 2]}>
      <mesh castShadow>
        <boxGeometry args={[config.width, SHELF_HEIGHT, config.height]} />
        <meshStandardMaterial color={SHELF_COLOR} opacity={0.85} transparent />
      </mesh>
      <Text
        position={[0, SHELF_HEIGHT / 2 + 0.3, 0]}
        rotation={[-Math.PI / 2, 0, 0]}
        fontSize={0.5}
        color="#333"
        anchorX="center"
        anchorY="middle"
      >
        {config.id}
      </Text>
    </group>
  )
}
