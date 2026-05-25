import { Grid } from '@react-three/drei'

interface FloorProps {
  width: number
  height: number
}

export function Floor({ width, height }: FloorProps) {
  return (
    <group position={[width / 2, 0, height / 2]}>
      {/* Ground plane */}
      <mesh rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
        <planeGeometry args={[width, height]} />
        <meshStandardMaterial color="#e8e8e8" />
      </mesh>
      {/* Grid overlay */}
      <Grid
        args={[width, height]}
        cellSize={1}
        cellThickness={0.5}
        cellColor="#d0d0d0"
        sectionSize={5}
        sectionThickness={1}
        sectionColor="#b0b0b0"
        fadeDistance={100}
        position={[0, 0.01, 0]}
      />
    </group>
  )
}
