import { Grid } from '@react-three/drei'

interface FloorProps {
  width: number
  height: number
}

export function Floor({ width, height }: FloorProps) {
  return (
    <group position={[width / 2, 0, height / 2]}>
      {/* Ground plane – light concrete */}
      <mesh rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
        <planeGeometry args={[width, height]} />
        <meshStandardMaterial color="#d8dce0" roughness={0.9} metalness={0.0} />
      </mesh>
      {/* Grid – clear warehouse lane markers */}
      <Grid
        args={[width, height]}
        cellSize={1}
        cellThickness={0.5}
        cellColor="#b0b8c4"
        sectionSize={5}
        sectionThickness={1.0}
        sectionColor="#8898a8"
        fadeDistance={120}
        position={[0, 0.015, 0]}
      />
    </group>
  )
}
